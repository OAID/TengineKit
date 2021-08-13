#include <jni.h>
#include <vector>
#include <android/log.h>
#include <string.h>
#include "iostream"
#include "faceService.h"

#ifndef FACE_MANAGER_H
#define FACE_MANAGER_H

class manager {
private:
public:
    manager();

    ~manager();

    void Init(const std::string &path);

    void detect(const unsigned char *img, const unsigned char *yuv, std::vector<FaceInfo> &faces,
                int width, int height, const FaceConfig& config);


    void release();


private:
    std::shared_ptr<faceService> mEngine;
};

#endif //FACE_MANAGER_H
