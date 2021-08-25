//
// Created by Hebing Shi on 2021/7/26.
//

#ifndef TENGINEDEMO_FACESERVICE_H
#define TENGINEDEMO_FACESERVICE_H


#include "../face/tenginekit_struct.hpp"

#include <iostream>
#include <memory>
#include <vector>
#include "c_api.h"
#include <string>

//Face
#include "../face/faceStruct.h"
#include "../face/faceDetect.hpp"
#include "../face/faceLandmark.hpp"
#include "../face/faceLandmark3d.hpp"
#include "../face/eyeLandmark.hpp"
#include "../face/faceAttribute.h"


class faceService {
private:
    ModelPathConfig mPathConfig;
    std::shared_ptr<faceDetect> detectHandler;
    std::shared_ptr<faceLandmark> landmarkHandler;
    std::shared_ptr<faceLandmark3d> landmark3dHandler;
    std::shared_ptr<eyeLandmark> eyeLandmarkHandler;
    std::shared_ptr<faceAttribute> faceAttributeHandler;
    bool hasInit = false;
    std::vector<uint8_t> buffer0;
    std::vector<uint8_t> buffer1;

    std::vector<uint8_t> yuvResizeBuffer;
    //std::vector<uint8_t> landmarkBuffer;


    std::vector<uint8_t> yuvCropBuffer;
    std::vector<uint8_t> landmark3dBuffer;
    bool hasSave = false;
    int saveCount = 0;

    void postProcessLandmark2d(FaceInfo &info, int left, int top, int width, int height, int iw, int ih);
public:
    faceService() = delete;

    faceService(ModelPathConfig faceConfig);

    void init();

    void runDetect(const uint8_t *input, const uint8_t *yuv, int iw, int ih,
                   std::vector<FaceInfo> &faceList, const FaceConfig &config);

    ~faceService();
};

#endif //TENGINEDEMO_FACESERVICE_H
