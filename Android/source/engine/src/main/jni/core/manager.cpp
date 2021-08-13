//
// Created by zhangjun on 2020/4/15.
//

#include "manager.hpp"
#include <iostream>
#include <memory>
#include <string>
#include <chrono>
#include <map>
#include <memory>


#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdlib>
#include <log.h>

using namespace std;

manager::manager() {
}

manager::~manager() {}

static std::map<FaceModelType, std::string> faceModelList = {{DETECT,     "/detect_sim_160_120.tmfile"},
                                                             {LANDMARK,   "/face_landmark_sim.tmfile"},
                                                             {LANDMARK3D, "/face_landmark3d.tmfile"},
                                                             {EYELANDMARK, "/iris_landmark.tmfile"},
};

void
manager::Init(const std::string &path) {
    ModelPathConfig config;
    config.detectModelPath = path + faceModelList[DETECT];
    config.landmarkModelPath = path + faceModelList[LANDMARK];
    config.landmark3dModelPath = path + faceModelList[LANDMARK3D];
    config.eyeLandmarkModelPath = path + faceModelList[EYELANDMARK];
    mEngine = std::make_shared<faceService>(config);
    mEngine->init();
}

void
manager::detect(const unsigned char *img, const unsigned char *yuv, std::vector<FaceInfo> &faces,
                int width, int height, const FaceConfig &config) {
    mEngine->runDetect(img, yuv, width, height, faces, config);
}

void manager::release() {
    mEngine.reset();
}
