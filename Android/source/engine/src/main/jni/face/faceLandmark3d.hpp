//
// Created by bli on 2021/8/4.
//

#ifndef FACELANDMARK3D_HPP
#define FACELANDMARK3D_HPP

#include <iostream>
#include <vector>
#include <memory>
#include <string.h>
#include <memory>
#include "data.hpp"

//Tengine
#include "c_api.h"
#include "faceDetect.hpp"

typedef struct FaceLandmark3dInfo {
    float landmarks3d[468 * 3];
} FaceLandmark3dInfo;


class faceLandmark3d {
public:
    faceLandmark3d(const std::string &modelPath, context_t context = nullptr, int num_thread = 2);

    ~faceLandmark3d();

    void landmark3d(const uint8_t *faceImage, FaceInfo &faceInfo);

    int landmark3dW = 192;
    int landmark3dH = 192;
    int landmark3dBpp = 3;

private:
    graph_t graph = nullptr;
    tensor_t inputTensor = nullptr;
    const float meanVal[3] = {127.5, 127.5, 127.5};
    const float normVal[3] = {1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5};
    int num_thread;
};


#endif //FACELANDMARK3D_HPP
