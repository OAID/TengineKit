#include "faceDetect.hpp"
#include "compiler_fp16.h"
#include <chrono>
#include <string>
#include <common/tengine_operations.h>
#include <log.h>

#define clip(x, y) (x < 0 ? 0 : (x > y ? y : x))

faceDetect::faceDetect(std::string model_path, context_t context, int numThread,
                       float score_threshold_, float iou_threshold_) {
    LOGE("modelPath is %s", model_path.c_str());
    //set
    top_graph = create_graph(nullptr, "tengine", model_path.c_str());
    if (top_graph == nullptr) {
        std::cout << "Create top graph failed\n";
    }
    input_tensor = get_graph_input_tensor(top_graph, 0, 0);
    int dims[] = {1, detectBpp, detectH, detectW};
    set_tensor_shape(input_tensor, dims, 4);

    //set end
    score_threshold = score_threshold_;
    iou_threshold = iou_threshold_;

    w_h_list = {detectW, detectH};

    for (auto size : w_h_list) {
        std::vector<float> fm_item;
        for (float stride : strides) {
            fm_item.push_back(ceil(size / stride));
        }
        featuremap_size.push_back(fm_item);
    }

    for (auto size : w_h_list) {
        shrinkage_size.push_back(strides);
    }
    /* generate prior anchors */
    for (int index = 0; index < num_featuremap; index++) {
        float scale_w = detectW / shrinkage_size[0][index];
        float scale_h = detectH / shrinkage_size[1][index];
        for (int j = 0; j < featuremap_size[1][index]; j++) {
            for (int i = 0; i < featuremap_size[0][index]; i++) {
                float x_center = (i + 0.5) / scale_w;
                float y_center = (j + 0.5) / scale_h;

                for (float k : min_boxes[index]) {
                    float w = k / detectW;
                    float h = k / detectH;
                    priors.push_back(
                            {clip(x_center, 1), clip(y_center, 1), clip(w, 1), clip(h, 1)});
                }
            }
        }
    }
    /* generate prior anchors finished */
    num_anchors = priors.size();
    struct options opt;
    opt.num_thread = numThread;
    opt.cluster = TENGINE_CLUSTER_ALL;
    opt.precision = TENGINE_MODE_FP32;
    opt.affinity = 0;
    if (0 != prerun_graph_multithread(top_graph, opt)) {
        std::cout << "Prerun top graph failed"  << "\n";
    }
}


void faceDetect::detect(const uint8_t *img, std::vector<FaceInfo> &face_list) {
    auto *input_img = new uint8_t[detectW * detectH * detectBpp];
    int hw = detectW * detectH;
    for (int w = 0; w < detectW; w++) {
        for (int h = 0; h < detectH; h++) {
            for (int c = 0; c < detectBpp; c++) {
                int tmp = int(*img);
                if (tmp > 255)
                    tmp = 255;
                else if (tmp < 0)
                    tmp = 0;
                input_img[c * hw + w * detectH + h] = tmp;
                img++;
            }
        }
    }

    inputData.resize(detectH * detectW * detectBpp);
    float *input = inputData.data();
    for (int c = 0; c < detectBpp; c++) {
        for (int i = 0; i < detectH; i++) {
            for (int j = 0; j < detectW; j++) {
                int index = c * detectH * detectW + i * detectW + j;
                input[index] = (input_img[index] - mean_vals[c]) * norm_vals[c];
            }
        }
    }

    set_tensor_buffer(input_tensor, inputData.data(),
                      detectW * detectH * detectBpp * sizeof(float));
    double start_time = get_cur_time();
    int ret = run_graph(top_graph, 1);
    double end_time = get_cur_time();
    LOGI("Detect Run Graph cost: %lf", (end_time - start_time));
    tensor_t tensor_scores = get_graph_tensor(top_graph, scores.c_str());
    tensor_t tensor_boxes = get_graph_tensor(top_graph, boxes.c_str());

    auto *boxs_data = (float *) get_tensor_buffer(tensor_boxes);
    auto *scores_data = (float *) get_tensor_buffer(tensor_scores);

    int dims_bbox_score[4] = {0};
    int dims_bbox_delta[4] = {0};

    int bbox_score_dims = get_tensor_shape(tensor_scores, dims_bbox_score, 4);
    int bbox_delta_dims = get_tensor_shape(tensor_boxes, dims_bbox_delta, 4);

    int score_size = 1, bbox_size = 1;
    for (int m = 0; m < bbox_delta_dims; m++)
        bbox_size *= dims_bbox_delta[m];

    for (int m = 0; m < bbox_score_dims; m++)
        score_size *= dims_bbox_score[m];

    std::vector<FaceInfo> bbox_collection;
    generateBBox(bbox_collection, scores_data, boxs_data);
    nms(bbox_collection, face_list);
    LOGI("facelist size = %d", face_list.size());
    delete[] input_img;
}

void faceDetect::generateBBox(std::vector<FaceInfo> &bbox_collection, float *scores,
                              float *boxes) {
    for (int i = 0; i < num_anchors; i++) {
        if (scores[i * 2 + 1] > score_threshold) {
            FaceInfo rects;
            float x_center =
                    boxes[i * 4] * center_variance * priors[i][2] + priors[i][0];
            float y_center =
                    boxes[i * 4 + 1] * center_variance * priors[i][3] + priors[i][1];
            float w = exp(boxes[i * 4 + 2] * size_variance) * priors[i][2];
            float h = exp(boxes[i * 4 + 3] * size_variance) * priors[i][3];

            rects.face_box.x1 = clip(x_center - w / 2.0, 1) * detectW;
            rects.face_box.y1 = clip(y_center - h / 2.0, 1) * detectH;
            rects.face_box.x2 = clip(x_center + w / 2.0, 1) * detectW;
            rects.face_box.y2 = clip(y_center + h / 2.0, 1) * detectH;
            rects.score = clip(scores[i * 2 + 1], 1);
            bbox_collection.push_back(rects);
        }
    }
}

void faceDetect::nms(std::vector<FaceInfo> &input, std::vector<FaceInfo> &output,
                     int type) {
    std::sort(input.begin(), input.end(),
              [](const FaceInfo &a, const FaceInfo &b) { return a.score > b.score; });

    int box_num = input.size();

    std::vector<int> merged(box_num, 0);

    for (int i = 0; i < box_num; i++) {
        if (merged[i])
            continue;
        std::vector<FaceInfo> buf;

        buf.push_back(input[i]);
        merged[i] = 1;

        float h0 = input[i].face_box.y2 - input[i].face_box.y1 + 1;
        float w0 = input[i].face_box.x2 - input[i].face_box.x1 + 1;

        float area0 = h0 * w0;

        for (int j = i + 1; j < box_num; j++) {
            if (merged[j])
                continue;

            float inner_x0 = input[i].face_box.x1 > input[j].face_box.x1 ? input[i].face_box.x1
                                                                         : input[j].face_box.x1;
            float inner_y0 = input[i].face_box.y1 > input[j].face_box.y1 ? input[i].face_box.y1
                                                                         : input[j].face_box.y1;

            float inner_x1 = input[i].face_box.x2 < input[j].face_box.x2 ? input[i].face_box.x2
                                                                         : input[j].face_box.x2;
            float inner_y1 = input[i].face_box.y2 < input[j].face_box.y2 ? input[i].face_box.y2
                                                                         : input[j].face_box.y2;

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

            if (score > iou_threshold) {
                merged[j] = 1;
                buf.push_back(input[j]);
            }
        }
        switch (type) {
            case hard_nms: {
                output.push_back(buf[0]);
                break;
            }
            case blending_nms: {
                float total = 0;
                for (int i = 0; i < buf.size(); i++) {
                    total += exp(buf[i].score);
                }
                FaceInfo rects;
                memset(&rects, 0, sizeof(rects));
                for (int i = 0; i < buf.size(); i++) {
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
            default: {
                printf("wrong type of nms.");
                exit(-1);
            }
        }
    }
}

faceDetect::~faceDetect() {
    release_graph_tensor(input_tensor);
    postrun_graph(top_graph);
    destroy_graph(top_graph);
}
