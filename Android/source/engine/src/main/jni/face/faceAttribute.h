//
// Created by Hebing Shi on 2021/8/17.
//

#ifndef TENGINEDEMO_FACEATTRIBUTE_H
#define TENGINEDEMO_FACEATTRIBUTE_H


#include <string>
#include <c_api.h>
#include "tenginekit_struct.hpp"

class faceAttribute {
public:
    faceAttribute(const std::string &modelPath,int num_thread_ = 2);
    ~faceAttribute();
    int attribute(const uint8_t* input, FaceInfo& faceInfo);

    int attributeW = 160;
    int attributeH = 160;
    int attributeC = 3;

private:
    graph_t graph = nullptr;
    tensor_t inputTensor = nullptr;
    int num_thread;
    int inputSize;

};


#endif //TENGINEDEMO_FACEATTRIBUTE_H
