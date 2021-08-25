//
// Created by Hebing Shi on 2021/7/26.
//

#include "faceService.h"

#include <utility>
#include <ImageResizeHelper.h>
#include <log.h>
#include <RGBConverterHelper.h>
#include <ImageRotateHelper.h>
#include <YuvConverterHelper.h>
#include "fstream"

#ifdef DEBUG_IMAGE

#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>

#endif

faceService::faceService(ModelPathConfig config) : mPathConfig(config) {
    LOGI("%s", __func__);
}

void faceService::init() {
    if (hasInit) return;
    hasInit = true;
    init_tengine();
    LOGI("tengine-lite library version: %s", get_tengine_version());
    detectHandler = std::make_shared<faceDetect>(mPathConfig.detectModelPath);
    landmarkHandler = std::make_shared<faceLandmark>(mPathConfig.landmarkModelPath);
    faceAttributeHandler = std::make_shared<faceAttribute>(mPathConfig.attributeModelPath);
}

void
faceService::runDetect(const uint8_t *input, const uint8_t *yuv, int iw, int ih,
                       std::vector<FaceInfo> &faceList, const FaceConfig &faceConfig) {
    buffer0.resize(iw * ih * 3);
    buffer1.resize(iw * ih * 3);
    LOGI("resize start");
    if (faceConfig.useYuvForResize) {
        ImageResizeHelper::resizeYuvBilinear(yuv, iw, ih, buffer1.data(),
                                             detectHandler->detectW, detectHandler->detectH);
        YuvConverterHelper::nv21RGB(buffer1.data(), buffer0.data(),
                                    detectHandler->detectW, detectHandler->detectH);
    } else {
        ImageResizeHelper::resize_bilinear_c3(input, iw, ih, iw * 3, buffer0.data(),
                                              detectHandler->detectW,
                                              detectHandler->detectH, detectHandler->detectW * 3);
    }
    LOGI("resize end");
    if (faceConfig.mirror) {
        ImageRotateHelper::mirrorC3(buffer0.data(), detectHandler->detectW,
                                    detectHandler->detectH,
                                    detectHandler->detectW * 3, buffer1.data(),
                                    detectHandler->detectW, detectHandler->detectH,
                                    detectHandler->detectW * 3);
        detectHandler->detect(buffer1.data(), faceList);
    } else {
        detectHandler->detect(buffer0.data(), faceList);
    }

    if (!faceList.empty()) {
        if (faceConfig.mirror) { ImageRotateHelper::mirrorNv(yuv, iw, ih, buffer1.data(), iw, ih); }
        for (int i = 0; i < faceList.size(); ++i) {
            //runLandMark
            float diff_x = (faceList[i].face_box.x2 - faceList[i].face_box.x1) * 0.2f;
            float diff_y = (faceList[i].face_box.y2 - faceList[i].face_box.y1) * 0.2f;
            int left = (faceList[i].face_box.x1 - diff_x) / 160.0f * iw;
            int top = (faceList[i].face_box.y1 - diff_y) / 120.0f * ih;
            int width = (faceList[i].face_box.x2 - faceList[i].face_box.x1 + diff_x * 2) /
                        160.0f * iw;
            int height = (faceList[i].face_box.y2 - faceList[i].face_box.y1 + diff_y * 2) /
                         120.0f * ih;

            int realLeft = left > 0 ? left : 0;
            int realTop = top > 0 ? top : 0;
            int realWidth = (realLeft + width) > iw ? iw - realLeft : width;
            int realHeight = (realTop + height) > ih ? ih - realTop : height;
            LOGI("final rect to crop: left:%d top:%d width:%d height:%d", realLeft, realTop,
                 realWidth,
                 realHeight);
            LOGI("crop start");
            yuvCropBuffer.resize(iw * ih * 3 / 2);
            ImageResizeHelper::cropYuv(faceConfig.mirror ? buffer1.data() : yuv,
                                       yuvCropBuffer.data(),
                                       iw, ih, realLeft, realTop, realWidth, realHeight);
            LOGI("crop end");
            LOGI("landmark resize start");
            ImageResizeHelper::resizeC1Bilinear(yuvCropBuffer.data(), realWidth, realHeight,
                                                buffer0.data(),
                                                landmarkHandler->landmarkW,
                                                landmarkHandler->landmarkH);
            LOGI("landmark resize end");
            landmarkHandler->landmark(buffer0.data(), faceList[i]);
            postProcessLandmark2d(faceList[i], realLeft, realTop, realWidth, realHeight, iw, ih);
            if (faceConfig.attribute) {
                ImageResizeHelper::resizeYuvBilinear(yuvCropBuffer.data(),
                                                     realWidth, realHeight, buffer0.data(),
                                                     faceAttributeHandler->attributeW,
                                                     faceAttributeHandler->attributeH);
                YuvConverterHelper::nv21RGB(buffer0.data(), buffer1.data(),
                                            faceAttributeHandler->attributeW,
                                            faceAttributeHandler->attributeH);
                faceAttributeHandler->attribute(buffer1.data(), faceList[i]);
            }
        }
    }
}


faceService::~faceService() {
    release_tengine();
}

void
faceService::postProcessLandmark2d(FaceInfo &info, int left, int top, int width, int height, int iw,
                                   int ih) {
    for (int j = 0; j < 212; ++j) {
        float x = (info.landmarks[j * 2] * (float) width + (float) left) /
                  (float) iw;
        float y =
                (info.landmarks[j * 2 + 1] * (float) height + (float) top) /
                (float) ih;
        info.landmarks[j * 2] = x;
        info.landmarks[j * 2 + 1] = y;
    }
}
