//
// Created by zhangjun on 2020/2/12.
//

#include "faceLandmark.hpp"
#include <string>
#include <vector>
#include <chrono>
#include <iostream>
#include <log.h>
#include "compiler_fp16.h"
#include "utils.hpp"
#include "tenginekit_api.hpp"

faceLandmark::faceLandmark(const std::string &modelPath, context_t context, int numThread) {
    num_thread = numThread;

    graph = create_graph(nullptr, "tengine", modelPath.c_str());
    int dims[] = {1, landmarkBpp, landmarkH, landmarkW};
    inputTensor = get_graph_input_tensor(graph, 0, 0);

    set_tensor_shape(inputTensor, dims, 4);

    struct options opt;
    opt.num_thread = numThread;
    opt.cluster = TENGINE_CLUSTER_ALL;
    opt.precision = TENGINE_MODE_FP32;
    opt.affinity = 0;

    if (0 != prerun_graph_multithread(graph, opt)) {
        std::cout << "Prerun graph failed " << "\n";
    }
    std::cout << "init Over" << std::endl;
}

void faceLandmark::landmark(const uint8_t *faceImage, FaceInfo &faceInfo) {
    auto *input_img = new float[landmarkW * landmarkH * landmarkBpp];
    auto *inputPointer = faceImage;
    int hw = landmarkW * landmarkH;
    for (int w = 0; w < landmarkW; w++) {
        for (int h = 0; h < landmarkH; h++) {
            for (int c = 0; c < landmarkBpp; c++) {
                int t = int(*inputPointer);
                if (t > 255)
                    t = 255;
                else if (t < 0)
                    t = 0;
                input_img[c * hw + w * landmarkH + h] = (t - meanVal[c]) * normVal[c];
                inputPointer++;
            }
        }
    }

    set_tensor_buffer(inputTensor, input_img,
                      landmarkW * landmarkH * landmarkBpp * sizeof(float));

    double start_time = get_cur_time();
    LOGI("landmark start");
    int ret = run_graph(graph, 1);
    double end_time = get_cur_time();
    std::cout << "Landmark Run Graph cost:" << (end_time - start_time) << std::endl;
    LOGI("landmark end");

    tensor_t tensor_output = get_graph_output_tensor(graph, 0, 0);
    auto *output_data = (float *) get_tensor_buffer(tensor_output);
    FaceLandmarkInfo out;
    memcpy(&out, (float *) output_data, sizeof(FaceLandmarkInfo));
    for (int i = 0; i < 212 * 2; ++i) {
        faceInfo.landmarks[i] = out.landmarks[i];
    }
    faceInfo.headX = out.headX * 90;
    faceInfo.headY = out.headY * 90;
    faceInfo.headZ = out.headZ * 90;
    faceInfo.leftEyeCloseState = sigmoid(out.leftEyeCloseState);
    faceInfo.rightEyeCloseState = sigmoid(out.rightEyeCloseState);
    faceInfo.mouthCloseState = sigmoid(out.mouthCloseState);
    faceInfo.mouthBigOpenState = sigmoid(out.mouthBigOpenState);
    delete[] input_img;
    std::cout << "Run Over" << std::endl;
}

faceLandmark::~faceLandmark() {
    release_graph_tensor(inputTensor);
    postrun_graph(graph);
    destroy_graph(graph);
}
