//
// Created by zhangjun on 2020/2/12.
//

#include "face_landmark.hpp"
#include <string>
#include <vector>
#include <chrono>
#include <iostream>
#include "compiler_fp16.h"
#include "utils.hpp"

face_landmark::face_landmark(std::string model_path, context_t context, int num_thread_)
{
    num_thread = num_thread_;

    graph = create_graph(context, "tengine", model_path.c_str());
    int dims[] = {1, bpp, landmark_h, landmark_w};
    input_tensor = get_graph_input_tensor(graph, 0, 0);

    set_tensor_shape(input_tensor, dims, 4);
    if (0 != prerun_graph(graph))
    {
        std::cout << "Prerun graph failed, errno: " << get_tengine_errno() << "\n";
    }
    std::cout << "init Over" << std::endl;
}

void face_landmark::landmark(std::vector<uint8_t *> face_list, std::vector<FaceLandmarkInfo> &landmarkOuts)
{
    for (int i = 0; i < face_list.size(); i++)
    {
        uint8_t *input_img = new uint8_t[landmark_w * landmark_h * bpp];
        int hw = landmark_w * landmark_h;
        for (int w = 0; w < landmark_w; w++)
        {
            for (int h = 0; h < landmark_h; h++)
            {
                for (int c = 0; c < bpp; c++)
                {
                    int t = int(*face_list[i]);
                    if (t > 255)
                        t = 255;
                    else if (t < 0)
                        t = 0;
                    input_img[c * hw + w * landmark_h + h] = t;

                    *face_list[i]++;
                }
            }
        }

        set_tensor_buffer(input_tensor, input_img, landmark_w * landmark_h * bpp * sizeof(uint8_t));

        double start_time = get_cur_time();
        int ret = run_graph(graph, 1);
        double end_time = get_cur_time();
        std::cout << "Landmark Run Graph cost:" << (end_time - start_time) << std::endl;

        tensor_t tensor_output = get_graph_output_tensor(graph, 0, 0);
        float *output_data = (float*)get_tensor_buffer(tensor_output);
        // uint8_t *output_data_uint8 = (uint8_t *)get_tensor_buffer(tensor_output);

        // int dims_output[4] = {0};
        // int bbox_score_dims = get_tensor_shape(tensor_output, dims_output, 4);
        // float output_scale = 0.0f;
        // int output_zero_point = 0;
        // int ret_param = get_tensor_quant_param(tensor_output, &output_scale, &output_zero_point, 1);
        // int output_size = 1;
        // for (int m = 0; m < bbox_score_dims; m++)
        //     output_size *= dims_output[m];

        // float *output_data = (float *)malloc(output_size * sizeof(float));
        // for (int u = 0; u < output_size; u++)
        //     output_data[u] = ((float)output_data_uint8[u]- output_zero_point) * output_scale;

        FaceLandmarkInfo out;
        memcpy(&out, (float*)output_data, sizeof(FaceLandmarkInfo));
        landmarkOuts.push_back(out);
        delete[] input_img;
        std::cout << "Run Over" << std::endl;
    }
}

face_landmark::~face_landmark()
{
    release_graph_tensor(input_tensor);
    postrun_graph(graph);
    destroy_graph(graph);
}
