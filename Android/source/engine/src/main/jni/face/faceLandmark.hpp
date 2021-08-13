//
// Created by zhangjun on 2020/2/12.
//

#ifndef FACE_LANDMARK_HPP
#define FACE_LANDMARK_HPP

#include <iostream>
#include <vector>
#include <memory>
#include <string.h>
#include <memory>
#include "data.hpp"

//Tengine
#include "c_api.h"
#include "faceDetect.hpp"

typedef struct FaceLandmarkInfo {
    float landmarks[212 * 2];
    float head_x;
    float head_y;
    float head_z;
    float lefteye_close_state;
    float righteye_close_state;
    float mouth_close_state;
    float mouth_bigopen_state;
} FaceLandmarkInfo;


class faceLandmark {
public:
    faceLandmark(const std::string& modelPath, context_t context = nullptr, int numThread = 2);
    ~faceLandmark();

    void landmark(const uint8_t *faceImage, FaceInfo &faceInfo);

    int landmarkW = 112;
    int landmarkH = 112;
    int landmarkBpp = 1;

private:
    graph_t graph = nullptr;
    tensor_t inputTensor = nullptr;
    const float meanVal[3] = {127.5, 127.5, 127.5};
    const float normVal[3] = {1.0 / 128.0, 1.0 / 128.0, 1.0 / 128.0};
    int num_thread;
};


#endif //FACE_LANDMARKS_HPP
