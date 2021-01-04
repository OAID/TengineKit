#include "face_detect.hpp"
#include "compiler_fp16.h"
#include <chrono>

#define clip(x, y) (x < 0 ? 0 : (x > y ? y : x))



face_detect::face_detect(std::string model_path, context_t context, int num_thread_, float score_threshold_, float iou_threshold_)
{
    /**
     * Tengine NPU Set
     * 
    */

    top_graph = create_graph(context, "tengine", model_path.c_str());
    if (top_graph == nullptr)
    {
        std::cout << "Create top graph failed\n";
    }
    input_tensor = get_graph_tensor(top_graph, "input");
    int dims[] = {1, detect_bpp, detect_h, detect_w};
    set_tensor_shape(input_tensor, dims, 4);

    /**** set End***/
    score_threshold = score_threshold_;
    iou_threshold = iou_threshold_;

    w_h_list = {detect_w, detect_h};

    for (auto size : w_h_list)
    {
        std::vector<float> fm_item;
        for (float stride : strides)
        {
            fm_item.push_back(ceil(size / stride));
        }
        featuremap_size.push_back(fm_item);
    }

    for (auto size : w_h_list)
    {
        shrinkage_size.push_back(strides);
    }
    /* generate prior anchors */
    for (int index = 0; index < num_featuremap; index++)
    {
        float scale_w = detect_w / shrinkage_size[0][index];
        float scale_h = detect_h / shrinkage_size[1][index];
        for (int j = 0; j < featuremap_size[1][index]; j++)
        {
            for (int i = 0; i < featuremap_size[0][index]; i++)
            {
                float x_center = (i + 0.5) / scale_w;
                float y_center = (j + 0.5) / scale_h;

                for (float k : min_boxes[index])
                {
                    float w = k / detect_w;
                    float h = k / detect_h;
                    priors.push_back(
                        {clip(x_center, 1), clip(y_center, 1), clip(w, 1), clip(h, 1)});
                }
            }
        }
    }
    /* generate prior anchors finished */

    num_anchors = priors.size();

    int ret2 = prerun_graph(top_graph);
    if (ret2 != 0)
    {
        std::cout << "Prerun top graph failed, errno: " << get_tengine_errno() << "\n";
    }
}


void face_detect::detect(uint8_t *img, std::vector<FaceDetectInfo> &face_list)
{
    uint8_t *input_img = new uint8_t[detect_w * detect_h * detect_bpp];
    int hw = detect_w * detect_h;
    for (int w = 0; w < detect_w; w++)
    {
        for (int h = 0; h < detect_h; h++)
        {
            for (int c = 0; c < detect_bpp; c++)
            {
                int tmp = int(*img);
                if (tmp > 255)
                    tmp = 255;
                else if (tmp < 0)
                    tmp = 0;
                input_img[c * hw + w * detect_h + h] = tmp;
                img++;
            }
        }
    }

    set_tensor_buffer(input_tensor, input_img, detect_w * detect_h * detect_bpp * sizeof(uint8_t));
    double start_time = get_cur_time();
    int ret = run_graph(top_graph, 1);
    double end_time = get_cur_time();
    std::cout << "Detect Run Graph cost:" << (end_time - start_time) << std::endl;
    tensor_t tensor_scores = get_graph_tensor(top_graph, scores.c_str());
    tensor_t tensor_boxes  = get_graph_tensor(top_graph, boxes.c_str());

    __fp16 *score_data_fp16 = (__fp16 *)get_tensor_buffer(tensor_scores);
    uint8_t *bbox_data_uint8 = (uint8_t *)get_tensor_buffer(tensor_boxes);
    int dims_bbox_score[4] = {0};
    int dims_bbox_delta[4] = {0};

    int bbox_score_dims = get_tensor_shape(tensor_scores, dims_bbox_score, 4);
    int bbox_delta_dims = get_tensor_shape(tensor_boxes,  dims_bbox_delta, 4);

    int score_size = 1, bbox_size = 1;
    for (int m = 0; m < bbox_delta_dims; m++)
        bbox_size *= dims_bbox_delta[m];

    for (int m = 0; m < bbox_score_dims; m++)
        score_size *= dims_bbox_score[m];

    float *score_data = (float *)malloc(score_size * sizeof(float));
    float *bbox_data  = (float *)malloc(bbox_size  * sizeof(float));

    float score_scale = 0.0f, bbox_scale = 0.0f;
    int score_zero_point = 0, bbox_zero_point = 0;
    get_tensor_quant_param(tensor_scores, &score_scale, &score_zero_point, 1);
    get_tensor_quant_param(tensor_boxes, &bbox_scale, &bbox_zero_point, 1);

    for (int i = 0; i < score_size; i++)
        score_data[i] = ((float)score_data_fp16[i]);// - score_zero_point) * score_scale;
    for (int i = 0; i < bbox_size; i++)
        bbox_data[i] = ((float)bbox_data_uint8[i] - bbox_zero_point) * bbox_scale;

    std::vector<FaceDetectInfo> bbox_collection;
    generateBBox(bbox_collection, score_data, bbox_data);
    nms(bbox_collection, face_list);
    std::cout << face_list.size() << std::endl;
    delete[] score_data;
    delete[] bbox_data;
    delete[] input_img;
    std::cout << "Run Over" << std::endl;
}

void face_detect::generateBBox(std::vector<FaceDetectInfo> &bbox_collection, float *scores, float *boxes)
{
    for (int i = 0; i < num_anchors; i++)
    {
        if (scores[i * 2 + 1] > score_threshold)
        {
            FaceDetectInfo rects;
            float x_center =
                boxes[i * 4] * center_variance * priors[i][2] + priors[i][0];
            float y_center =
                boxes[i * 4 + 1] * center_variance * priors[i][3] + priors[i][1];
            float w = exp(boxes[i * 4 + 2] * size_variance) * priors[i][2];
            float h = exp(boxes[i * 4 + 3] * size_variance) * priors[i][3];

            rects.face_box.x1 = clip(x_center - w / 2.0, 1) * detect_w;
            rects.face_box.y1 = clip(y_center - h / 2.0, 1) * detect_h;
            rects.face_box.x2 = clip(x_center + w / 2.0, 1) * detect_w;
            rects.face_box.y2 = clip(y_center + h / 2.0, 1) * detect_h;
            rects.score = clip(scores[i * 2 + 1], 1);
            bbox_collection.push_back(rects);
        }
    }
}

void face_detect::nms(std::vector<FaceDetectInfo> &input, std::vector<FaceDetectInfo> &output, int type)
{
    std::sort(input.begin(), input.end(),
              [](const FaceDetectInfo &a, const FaceDetectInfo &b) { return a.score > b.score; });

    int box_num = input.size();

    std::vector<int> merged(box_num, 0);

    for (int i = 0; i < box_num; i++)
    {
        if (merged[i])
            continue;
        std::vector<FaceDetectInfo> buf;

        buf.push_back(input[i]);
        merged[i] = 1;

        float h0 = input[i].face_box.y2 - input[i].face_box.y1 + 1;
        float w0 = input[i].face_box.x2 - input[i].face_box.x1 + 1;

        float area0 = h0 * w0;

        for (int j = i + 1; j < box_num; j++)
        {
            if (merged[j])
                continue;

            float inner_x0 = input[i].face_box.x1 > input[j].face_box.x1 ? input[i].face_box.x1 : input[j].face_box.x1;
            float inner_y0 = input[i].face_box.y1 > input[j].face_box.y1 ? input[i].face_box.y1 : input[j].face_box.y1;

            float inner_x1 = input[i].face_box.x2 < input[j].face_box.x2 ? input[i].face_box.x2 : input[j].face_box.x2;
            float inner_y1 = input[i].face_box.y2 < input[j].face_box.y2 ? input[i].face_box.y2 : input[j].face_box.y2;

            float inner_h = inner_y1 - inner_y0 + 1;
            float inner_w = inner_x1 - inner_x0 + 1;

            if (inner_h <= 0 || inner_w <= 0)
                continue;

            float inner_area = inner_h * inner_w;

            float h1 = input[j].face_box.y2 - input[j].face_box.y1 + 1;
            float w1 = input[j].face_box.x2 - input[j].face_box.x1 + 1;

            float area1 = h1 * w1;

            float score;

            score = inner_area / (area0 + area1 - inner_area);

            if (score > iou_threshold)
            {
                merged[j] = 1;
                buf.push_back(input[j]);
            }
        }
        switch (type)
        {
        case hard_nms:
        {
            output.push_back(buf[0]);
            break;
        }
        case blending_nms:
        {
            float total = 0;
            for (int i = 0; i < buf.size(); i++)
            {
                total += exp(buf[i].score);
            }
            FaceDetectInfo rects;
            memset(&rects, 0, sizeof(rects));
            for (int i = 0; i < buf.size(); i++)
            {
                float rate = exp(buf[i].score) / total;
                rects.face_box.x1 += buf[i].face_box.x1 * rate;
                rects.face_box.y1 += buf[i].face_box.y1 * rate;
                rects.face_box.x2 += buf[i].face_box.x2 * rate;
                rects.face_box.y2 += buf[i].face_box.y2 * rate;
                rects.score += buf[i].score * rate;
            }
            output.push_back(rects);
            break;
        }
        default:
        {
            printf("wrong type of nms.");
            exit(-1);
        }
        }
    }
}

face_detect::~face_detect()
{
    release_graph_tensor(input_tensor);
    postrun_graph(top_graph);
    destroy_graph(top_graph);
}
