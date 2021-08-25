//
// Created by Hebing Shi on 2021/8/17.
//

#include <iostream>
#include <log.h>
#include <vector>
#include "faceAttribute.h"


faceAttribute::faceAttribute(const std::string &modelPath, int num_thread_) {
    num_thread = num_thread_;
    inputSize = attributeC * attributeW * attributeH;
    graph = create_graph(nullptr, "tengine", modelPath.c_str());
    int dims[] = {1, attributeC, attributeH, attributeW};

    inputTensor = get_graph_input_tensor(graph, 0, 0);
    set_tensor_shape(inputTensor, dims, 4);
    struct options opt;
    opt.num_thread = num_thread;
    opt.cluster = TENGINE_CLUSTER_ALL;
    opt.precision = TENGINE_MODE_FP32;
    opt.affinity = 0;
    if (0 != prerun_graph_multithread(graph, opt)) {
        std::cout << "Prerun top graph failed" << "\n";
    }
}

faceAttribute::~faceAttribute() {
    release_graph_tensor(inputTensor);
    postrun_graph(graph);
    destroy_graph(graph);
}

int faceAttribute::attribute(const uint8_t *input, FaceInfo &faceInfo) {
    auto inputData = new float[attributeC * attributeW * attributeH];
    for (int c = 0; c < attributeC; ++c) {
        for (int w = 0; w < attributeW; ++w) {
            for (int h = 0; h < attributeH; ++h) {
                inputData[c * attributeW * attributeH + h * attributeW + w] =
                        (float) (input[h * attributeW * attributeC + w * attributeC + c]);
            }
        }
    }

    if (set_tensor_buffer(inputTensor, inputData, inputSize * sizeof(float)) < 0) {
        LOGE("Set input tensor buffer failed\n");
        return -1;
    }

    LOGI("run attribute start");
    if (run_graph(graph, 1) < 0) {
        LOGE("Run graph failed\n");
        return -1;
    }
    LOGI("run attribute end");

    tensor_t outputTensor0 = get_graph_output_tensor(graph, 0, 0); //age_smile
    tensor_t outputTensor1 = get_graph_output_tensor(graph, 1, 0); //eye_gaze
    tensor_t outputTensor2 = get_graph_output_tensor(graph, 2, 0); //beauty
    tensor_t outputTensor3 = get_graph_output_tensor(graph, 3, 0); //gender
    tensor_t outputTensor4 = get_graph_output_tensor(graph, 4, 0); //glasses

    auto *dataAgeSmile = (float *) (get_tensor_buffer(outputTensor0));
    auto *dataEyeGaze = (float *) (get_tensor_buffer(outputTensor1));
    auto *dataBeauty = (float *) (get_tensor_buffer(outputTensor2));
    auto *dataGender = (float *) (get_tensor_buffer(outputTensor3));
    auto *dataGlasses = (float *) (get_tensor_buffer(outputTensor4));

    if (dataGender[0] > 0.5)
        faceInfo.attribution.gender = 1;
    else
        faceInfo.attribution.gender = 0; // man

//    LOGI("gender %f %f", dataGender[0], dataGender[1]);
//    LOGI("agesmile %f %f", dataAgeSmile[0], dataAgeSmile[1]);
//    LOGI("beauty %f %f", dataBeauty[0], dataBeauty[1]);

    std::vector<float> glasses;
    glasses.push_back(dataGlasses[0]);
    glasses.push_back(dataGlasses[1]);
    glasses.push_back(dataGlasses[2]);
    std::sort(glasses.begin(), glasses.end(), [](int a, int b) {
        return a > b;
    });
    int index_glasses = 0;
    while (1) {
        if (dataGlasses[index_glasses] == glasses[0])
            break;
        index_glasses++;
    }
    faceInfo.attribution.glasses = index_glasses;
    faceInfo.attribution.age = dataAgeSmile[0] * 100.f;
    faceInfo.attribution.smile = dataAgeSmile[1] * 100.f;
    faceInfo.attribution.beauty_man_look = dataBeauty[0] * 100.f;
    faceInfo.attribution.beauty_woman_look = dataBeauty[1] * 100.f;

    delete[] inputData;
    LOGI("age:%d gender:%d", faceInfo.attribution.age, faceInfo.attribution.gender);

    return 0;
}
