#include "face_image.hpp"
#include <iostream>

face_image::face_image(/* args */)
{
}

void face_image::handDetectImage(uint8_t *input, int iw, int ih, uint8_t *output, int ow, int oh)
{
    cv::Mat MInput(ih, iw, CV_8UC3, input);
    cv::cvtColor(MInput, MInput, CV_BGR2RGB);
    cv::resize(MInput, MInput, cv::Size(ow, oh));
    memcpy(output, MInput.data, ow * oh * 3);
}

void face_image::handleLandmark(uint8_t *input, int iw, int ih, std::vector<FaceDetectInfo> detectinfo, std::vector<uint8_t *> &landmarkdata, int ow, int oh)
{
    cv::Mat MInput (ih, iw, CV_8UC3, input);
    for (int i = 0; i < detectinfo.size(); i++)
    {
        float diff_x = (detectinfo[i].face_box.x2 - detectinfo[i].face_box.x1) * 0.2f;
        float diff_y = (detectinfo[i].face_box.y2 - detectinfo[i].face_box.y1) * 0.2f;
        cv::Rect rect((detectinfo[i].face_box.x1 - diff_x) / 160.0f * iw,
                    (detectinfo[i].face_box.y1 - diff_y) / 120.0f * ih,
                    (detectinfo[i].face_box.x2 - detectinfo[i].face_box.x1 + diff_x * 2) / 160.0f * iw,
                    (detectinfo[i].face_box.y2 - detectinfo[i].face_box.y1 + diff_y * 2) / 120.0f * ih);
        cv::Mat dst = cv::Mat (MInput, rect);
        cv::resize(dst, dst, cv::Size(ow, oh));
        cv::cvtColor(dst, dst, CV_BGR2GRAY);
        uint8_t *output_data = new uint8_t[ow * oh];
        memcpy(output_data, dst.data, ow * oh * 1);
        landmarkdata.push_back(output_data);
    }
}

face_image::~face_image()
{
}
