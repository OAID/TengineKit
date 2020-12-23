#ifndef MANAGER_HPP
#define MANAGER_HPP

#include <iostream>
#include <memory>
#include <vector>
#include "face_detect.hpp"
#include "face_image.hpp"
#include "face_landmark.hpp"
#include "tengine_c_api.h"
#include <string>

class manager
{
private:
    context_t vx_context;
    std::shared_ptr<face_detect> detect_handler;
    std::shared_ptr<face_image> image_handler;
    std::shared_ptr<face_landmark> landmark_handler;
    bool inited = false;
public:
    manager(/* args */);
    void init();
    void runDetect(uint8_t* input, int iw, int ih, std::vector<FaceDetectInfo> &face_list);
    void runLandmark2d(uint8_t* img, int w, int h, std::vector<FaceDetectInfo> detectinfo, std::vector<FaceLandmarkInfo> &info);
    ~manager();
};
#endif