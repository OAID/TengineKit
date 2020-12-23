#ifndef FACE_IMAGE_HPP
#define FACE_IMAGE_HPP

#include <iostream>
#include "opencv2/opencv.hpp"
#include <memory>
#include <face_detect.hpp>

class face_image
{
public:
    face_image(/* args */);
    void handDetectImage(uint8_t* input, int iw, int ih, uint8_t* output, int ow, int oh);
    void handleLandmark(uint8_t* input, int iw, int ih, std::vector<FaceDetectInfo> detectinfo, std::vector<uint8_t*> &landmarkdata, int ow, int oh);
    ~face_image();
};

#endif