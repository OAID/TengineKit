//
// Created by Hebing Shi on 2021/7/29.
//

#ifndef TENGINEDEMO_FACESTRUCT_H
#define TENGINEDEMO_FACESTRUCT_H

#include <string>


struct ModelPathConfig {
    std::string detectModelPath;
    std::string landmarkModelPath;
    std::string landmark3dModelPath;
    std::string eyeLandmarkModelPath;
};

struct FaceConfig {
    bool detect = true;
    bool landmark = true;
    bool landmark3d = false;
    bool eyeLandmark = false;
    bool useYuvForResize = false;
    bool mirror = false;
};

enum ImageFormat {
    YUV = 0,
    RGB = 1,
    RGBA = 2
};


#endif //TENGINEDEMO_FACESTRUCT_H
