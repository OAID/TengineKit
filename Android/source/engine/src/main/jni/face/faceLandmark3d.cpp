//
// Created by bli on 2021/8/4.
//

#include "faceLandmark3d.hpp"
#include <string>
#include <vector>
#include <chrono>
#include <iostream>
#include <log.h>
#include "compiler_fp16.h"
#include "utils.hpp"
#include "tenginekit_api.hpp"

faceLandmark3d::faceLandmark3d(const std::string &modelPath, context_t context, int num_thread_) {
    num_thread = num_thread_;
    graph = create_graph(nullptr, "tengine", modelPath.c_str());
    int dims[] = {1, landmark3dBpp, landmark3dH, landmark3dW};
    inputTensor = get_graph_input_tensor(graph, 0, 0);
    set_tensor_shape(inputTensor, dims, 4);

    struct options opt;
    opt.num_thread = num_thread_;
    opt.cluster = TENGINE_CLUSTER_ALL;
    opt.precision = TENGINE_MODE_FP32;
    opt.affinity = 0;
    if (0 != prerun_graph_multithread(graph, opt)) {
        std::cout << "Prerun facelandmark3d graph failed " << "\n";
    }
    std::cout << "init Over" << std::endl;
}

void faceLandmark3d::landmark3d(const uint8_t *faceImage, FaceInfo &faceInfo) {
    auto *input_img = new float[landmark3dW * landmark3dH * landmark3dBpp];
    auto *inputPointer = faceImage;
    int hw = landmark3dW * landmark3dH;
//    for (int w = 0; w < landmark3dW; w++) {
//        for (int h = 0; h < landmark3dH; h++) {
//            for (int c = 0; c < landmark3dBpp; c++) {
//                int t = int(*inputPointer);
//                if (t > 255)
//                    t = 255;
//                else if (t < 0)
//                    t = 0;
//                input_img[c * hw + w * landmark3dH + h] = (t - meanVal[c]) * normVal[c];
//                inputPointer++;
//            }
//        }
//    }

    for (int h = 0; h < landmark3dH; ++h) {
        for (int w = 0; w < landmark3dW; ++w) {
            for (int c = 0; c < 3; ++c) {
                int t = int(*inputPointer);
                if (t > 255)
                    t = 255;
                else if (t < 0)
                    t = 0;
                input_img[c * hw + h * landmark3dW + w] = 0;
                inputPointer++;
            }
        }
    }

//    for (int i = 0; i < 3; ++i) {
//        for (int j = 0; j < hw; ++j) {
//            int t = int(*inputPointer);
//            if (t > 255)
//                t = 255;
//            else if (t < 0)
//                t = 0;
//            input_img[i * hw + j] = (t - meanVal[i]) * normVal[i];
//            inputPointer++;
//        }
//    }


    set_tensor_buffer(inputTensor, input_img,
                      landmark3dW * landmark3dH * landmark3dBpp * sizeof(float));

    double start_time = get_cur_time();
    LOGI("landmark3d start");
    int ret = run_graph(graph, 1);
    double end_time = get_cur_time();
    std::cout << "landmark3d Run Graph cost:" << (end_time - start_time) << std::endl;
    LOGI("landmark3d end");

    tensor_t tensor_output = get_graph_output_tensor(graph, 1, 0);
    auto *output_data = (float *) get_tensor_buffer(tensor_output);
//    FaceLandmark3dInfo out;
    memcpy(faceInfo.landmarks3d, (float *) output_data, sizeof(FaceLandmark3dInfo));
//    for (int i = 0; i < 468; i++) {
//        faceInfo.landmarks3d[i * 3 + 0] = faceInfo.landmarks3d[i * 3 + 0] / (landmark3dW * 1.0);
//        faceInfo.landmarks3d[i * 3 + 1] = faceInfo.landmarks3d[i * 3 + 1] / (landmark3dH * 1.0);
//    }

    for (int i = 0; i < 468; ++i) {
        LOGE("%f %f", faceInfo.landmarks3d[3 * i],faceInfo.landmarks3d[3*i + 1] );
    }

//todo
// return FaceInfo3d
    delete[] input_img;
    std::cout << "Run Over" << std::endl;
}

faceLandmark3d::~faceLandmark3d() {
    release_graph_tensor(inputTensor);
    postrun_graph(graph);
    destroy_graph(graph);
}
