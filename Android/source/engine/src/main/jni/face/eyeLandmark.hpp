//
// Created by bli on 2021/8/5.
//

#ifndef EYELANDMARK_HPP
#define EYELANDMARK_HPP

#include <iostream>
#include <vector>
#include <memory>
#include <string.h>
#include <memory>
#include "data.hpp"

//Tengine
#include "c_api.h"
#include "faceDetect.hpp"

#define EYE_LANDMARK_NUM 71
#define EYE_IRIIS_NUM 5

typedef struct EyeOutInfo {
    float eyelandmark[EYE_LANDMARK_NUM * 3];
    float eyeiris[EYE_IRIIS_NUM * 3];
} EyeLandmarkInfo;


class eyeLandmark {
public:
    eyeLandmark(const std::string &modelPath, context_t context = nullptr, int num_thread = 2);

    void landmarkEye(const uint8_t *faceImage, FaceInfo &faceInfo);;

    ~eyeLandmark();

    int eyeLandmarkW = 64;
    int eyeLandmarkH = 64;
    int eyeLandmarkBpp = 3;


private:
    graph_t graph = nullptr;
    tensor_t inputTensor = nullptr;
    const float meanVal[3] = {0.0f, 0.0f, 0.0f};
    const float normVal[3] = {1.0 / 255.0f, 1.0 / 255.0f, 1.0 / 255.0f};
    int num_thread;
    std::vector<float> inputData;
};


#endif //EYELANDMARK_HPP
