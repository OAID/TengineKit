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
#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>

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
    landmark3dHandler = std::make_shared<faceLandmark3d>(mPathConfig.landmark3dModelPath);
    eyeLandmarkHandler = std::make_shared<eyeLandmark>(mPathConfig.eyeLandmarkModelPath);
}

void
faceService::runDetect(const uint8_t *input, const uint8_t *yuv, int iw, int ih,
                       std::vector<FaceInfo> &faceList, const FaceConfig &faceConfig) {
    rgbBuffer.resize(detectHandler->detectW * detectHandler->detectH * 3);
    mirrorBuffer.resize(detectHandler->detectW * detectHandler->detectH * 3);
    LOGI("resize start");
    if (faceConfig.useYuvForResize) {
        yuvResizeBuffer.resize(detectHandler->detectH * detectHandler->detectW * 3 / 2);
        ImageResizeHelper::resizeYuvBilinear(yuv, iw, ih, yuvResizeBuffer.data(),
                                             detectHandler->detectW, detectHandler->detectH);
        YuvConverterHelper::nv21RGB(yuvResizeBuffer.data(), rgbBuffer.data(),
                                    detectHandler->detectW, detectHandler->detectH);
#ifdef DEBUG_IMAGE
        if (!hasSave && saveCount > 100) {
            cv::Mat yuvResize(detectHandler->detectH * 3 / 2, detectHandler->detectW, CV_8UC1,
                              yuvResizeBuffer.data());
            cv::imwrite(
                    "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/yuvResize.png",
                    yuvResize);


            cv::Mat rgbAll(detectHandler->detectH, detectHandler->detectW, CV_8UC3,
                           rgbBuffer.data());
            cv::imwrite(
                    "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/rgbAll.png",
                    rgbAll);
            //hasSave = true;
        }
#endif
    } else {
        ImageResizeHelper::resize_bilinear_c3(input, iw, ih, iw * 3, rgbBuffer.data(),
                                              detectHandler->detectW,
                                              detectHandler->detectH, detectHandler->detectW * 3);
    }
    LOGI("resize end");
    if (faceConfig.mirror) {
        ImageRotateHelper::mirrorC3(rgbBuffer.data(), detectHandler->detectW,
                                    detectHandler->detectH,
                                    detectHandler->detectW * 3, mirrorBuffer.data(),
                                    detectHandler->detectW, detectHandler->detectH,
                                    detectHandler->detectW * 3);
        detectHandler->detect(mirrorBuffer.data(), faceList);
    } else {
        detectHandler->detect(rgbBuffer.data(), faceList);
    }

    if (!faceList.empty()) {
        saveCount++;
        uint8_t *mirrorY = nullptr;
        if (faceConfig.mirror) {
            mirrorY = (uint8_t *) malloc(iw * ih);
            LOGI("mirror start");
            ImageRotateHelper::mirrorC1(
                    yuv, iw, ih, iw, mirrorY, iw, ih, iw
            );
            LOGI("mirror end");
        }
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
            auto cropOutput = (uint8_t *) malloc(realWidth * realHeight);
            cropYuv(faceConfig.mirror ? mirrorY : yuv, cropOutput, iw, realLeft, realTop, realWidth,
                    realHeight);
            LOGI("crop end");
            landmarkBuffer.resize(landmarkHandler->landmarkH * landmarkHandler->landmarkW *
                                  landmarkHandler->landmarkBpp);
            ImageResizeHelper::resizeC1Bilinear(cropOutput, realWidth, realHeight,
                                                landmarkBuffer.data(),
                                                landmarkHandler->landmarkW,
                                                landmarkHandler->landmarkH);
            LOGI("resize end");
            landmarkHandler->landmark(landmarkBuffer.data(), faceList[i]);
            for (int j = 0; j < 212; ++j) {
                float x = (faceList[i].landmarks[j * 2] * (float) realWidth + (float) realLeft) /
                          (float) iw;
                float y =
                        (faceList[i].landmarks[j * 2 + 1] * (float) realHeight + (float) realTop) /
                        (float) ih;
                faceList[i].landmarks[j * 2] = x;
                faceList[i].landmarks[j * 2 + 1] = y;
            }
#ifdef DEBUG_IMAGE
            if (!hasSave && !faceList.empty() && saveCount > 100) {
                cv::Mat mY(ih, iw, CV_8UC1);
                memcpy(mY.data, faceConfig.mirror ? mirrorY : yuv, ih * iw);
                cv::Point pt1(left, top);
                cv::Point pt2(left + width, top + height);
                cv::rectangle(mY, pt1, pt2, cv::Scalar(255), 1);
                pt1.x = realLeft;
                pt1.y = realTop;
                pt2.x = realLeft + realWidth;
                pt2.y = realTop + height;
                cv::rectangle(mY, pt1, pt2, cv::Scalar(255), 1);
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/mY.png",
                        mY);


                cv::Mat cropIm(realHeight, realWidth, CV_8UC1, cropOutput);
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/runCrop.png",
                        cropIm);


                cv::Mat landmarkIm(landmarkHandler->landmarkH, landmarkHandler->landmarkW, CV_8UC1,
                                   landmarkBuffer.data());
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/runLandMark.png",
                        landmarkIm);

                cv::Mat dst(detectHandler->detectH, detectHandler->detectW, CV_8UC3,
                            faceConfig.mirror ? mirrorBuffer.data() : rgbBuffer.data());
                for (int i = 0; i < faceList.size(); ++i) {
                    FaceInfo info = faceList[i];
                    LOGE("%f %f %f %f", info.face_box.x1, info.face_box.y1, info.face_box.x2,
                         info.face_box.y2);
                    cv::Point pt1(info.face_box.x1, info.face_box.y1);
                    cv::Point pt2(info.face_box.x2, info.face_box.y2);
                    cv::rectangle(dst, pt1, pt2, cv::Scalar(0, 255, 0), 2);
                }
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/runDetect.png",
                        dst);
                hasSave = true;
            }
#endif
            free(cropOutput);
        }

        if (faceConfig.mirror) {
            free(mirrorY);
        }
    }
}


faceService::~faceService() {
    release_tengine();
}

void faceService::cropYuv(const uint8_t *input, uint8_t *output, int inputWidth, int left, int top,
                          int width,
                          int height) {
    const uint8_t *src = input + top * inputWidth + left;
    uint8_t *dst = output;
    for (int i = 0; i < height; ++i) {
        memcpy(dst, src, width);
        src += inputWidth;
        dst += width;
    }
}
