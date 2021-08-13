//
// Created by bli on 2021/8/5.
//

#include "eyeLandmark.hpp"
#include <string>
#include <vector>
#include <chrono>
#include <iostream>
#include <log.h>
#include "compiler_fp16.h"
#include "utils.hpp"
#include "tenginekit_api.hpp"

eyeLandmark::eyeLandmark(const std::string &modelPath, context_t context, int num_thread_) {
    num_thread = num_thread_;
    graph = create_graph(nullptr, "tengine", modelPath.c_str());
    int dims[] = {1, eyeLandmarkBpp, eyeLandmarkH, eyeLandmarkW};
    inputTensor = get_graph_input_tensor(graph, 0, 0);
    set_tensor_shape(inputTensor, dims, 4);
    if (0 != prerun_graph(graph)) {
        std::cout << "Prerun graph failed" << "\n";
    }
    std::cout << "init Over" << std::endl;
}

void eyeLandmark::landmarkEye(const uint8_t *faceImage, FaceInfo &faceInfo) {
    auto *input_img = new float[eyeLandmarkW * eyeLandmarkH * eyeLandmarkBpp];
    auto *inputPointer = faceImage;
    int hw = eyeLandmarkW * eyeLandmarkH;
    for (int w = 0; w < eyeLandmarkW; w++) {
        for (int h = 0; h < eyeLandmarkH; h++) {
            for (int c = 0; c < eyeLandmarkBpp; c++) {
                int t = int(*inputPointer);
                if (t > 255)
                    t = 255;
                else if (t < 0)
                    t = 0;
                input_img[c * hw + w * eyeLandmarkH + h] = (t - meanVal[c]) * normVal[c];
                inputPointer++;
            }
        }
    }

    set_tensor_buffer(inputTensor, input_img,
                      eyeLandmarkW * eyeLandmarkH * eyeLandmarkBpp * sizeof(float));

    double start_time = get_cur_time();
    LOGI("eyeLandmark start");
    int ret = run_graph(graph, 1);
    double end_time = get_cur_time();
    std::cout << "eyeLandmark Run Graph cost:" << (end_time - start_time) << std::endl;
    LOGI("eyeLandmark end");

    tensor_t tensorEye = get_graph_output_tensor(graph, 0, 0); //output_eyes_contours_and_brows
    tensor_t tensorIris = get_graph_output_tensor(graph, 1, 0); //output_iris

    float *eyeData = (float *) get_tensor_buffer(tensorEye);
    float *irisData = (float *) get_tensor_buffer(tensorIris);

    EyeLandmarkInfo out;

    memcpy(&out.eyelandmark, (float *) eyeData, EYE_LANDMARK_NUM * 3 * sizeof(float));
    memcpy(&out.eyeiris, (float *) irisData, EYE_IRIIS_NUM * 3 * sizeof(float));
    for (int i = 0; i < EYE_LANDMARK_NUM; i++) {
        out.eyelandmark[i * 3 + 0] = out.eyelandmark[i * 3 + 0] / (eyeLandmarkW * 1.0);
        out.eyelandmark[i * 3 + 1] = out.eyelandmark[i * 3 + 1] / (eyeLandmarkH * 1.0);
    }
    for (int i = 0; i < EYE_IRIIS_NUM; i++) {
        out.eyeiris[i * 3 + 0] = out.eyeiris[i * 3 + 0] / (eyeLandmarkW * 1.0);
        out.eyeiris[i * 3 + 1] = out.eyeiris[i * 3 + 1] / (eyeLandmarkH * 1.0);
    }


//todo
// return EYE_LANDMARK
    delete[] input_img;
    std::cout << "Run Over" << std::endl;
}

eyeLandmark::~eyeLandmark() {
    release_graph_tensor(inputTensor);
    postrun_graph(graph);
    destroy_graph(graph);
}