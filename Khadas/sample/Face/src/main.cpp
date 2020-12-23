#include <iostream>
#include <opencv2/opencv.hpp>
#include <memory>
#include <chrono>
#include "manager.hpp"
#include <vector>


int main(int argc, char **argv)
{
    cv::Mat frame = cv::imread(argv[1]);
    manager *tm = new manager();
    int image_w = frame.cols;
    int image_h = frame.rows;
    cv::Mat origin;
    frame.copyTo(origin);
    origin = frame.clone();
    tm->init();
    std::vector<FaceDetectInfo> info;
    tm->runDetect(origin.data, image_w, image_h, info);
    
    std::vector<FaceLandmarkInfo> landmarkinfo;
    tm->runLandmark2d(origin.data, image_w, image_h, info, landmarkinfo);

    for (int i = 0; i < info.size(); i++)
    {
        float x1 = info[i].face_box.x1;
        float x2 = info[i].face_box.x2;
        float y1 = info[i].face_box.y1;
        float y2 = info[i].face_box.y2;

        cv::Point pt1(x1 / 160.0f * image_w, y1 / 120.0f * image_h);
        cv::Point pt2(x2 / 160.0f * image_w, y2 / 120.0f * image_h);
        cv::rectangle(frame, pt1, pt2, cv::Scalar(255, 0, 0), 2);
    }

    for (int j = 0; j < landmarkinfo.size(); j++)
    {
        float w = info[j].face_box.x2 - info[j].face_box.x1;
        float h = info[j].face_box.y2 - info[j].face_box.y1;
        for (int i = 0; i < 424; i = i + 2)
        {
            float point_x = (landmarkinfo[j].landmarks[i] * w * 1.4f - w * 0.2f + info[j].face_box.x1) / 160.0f * image_w;
            float point_y = (landmarkinfo[j].landmarks[i + 1] * h * 1.4f - h * 0.2f + info[j].face_box.y1) / 120.0f * image_h;
            cv::Point pt(point_x, point_y);
            cv::circle(frame, pt, 1, cv::Scalar(255, 0, 0), 1);
        }
    }
    cv::imwrite("FaceOutput.jpg", frame);

    return 0;
}