#include <iostream>
#include <opencv2/opencv.hpp>
#include <fstream>
#include <streambuf>
#include "tenginekit_api.h"
#include <chrono>

#include <stdio.h>
#include <sys/types.h>
#include <dirent.h>
#include <string>

FaceSDKConfig getConfig(int w, int h)
{
    FaceSDKConfig config;
    config.img_w = w;
    config.img_h = h;
    config.screen_w = w;
    config.screen_h = h;
    config.input_format = ImageFormat::BGR;
    config.mode = FaceSDKMode::Normal;
    config.thread_num = 2;
    return config;
}


void handle_rgb(std::string path)
{
    float eye_close_v = 0.75f;
    std::string image_file = path;
    cv::Mat frame = cv::imread(image_file);

    int w = frame.cols;
    int h = frame.rows;

    FaceSDKConfig config = getConfig(w, h);
    facesdk_init(config);

    char data[w * h * 3];
    memcpy(data, (char *)frame.data, w * h * 3);
    facesdk_readModelFromFile(ModelType::Detect, "models/face_detect.bin", ImageFormat::RGB);
    sdkFaces faces = facesdk_detect(data);
    std::cout << "faces:" << faces.face_count << std::endl;
    for (int i = 0; i < faces.face_count; i++)
    {
        cv::Point pt1(faces.info[i].face_box.x1, faces.info[i].face_box.y1);
        cv::Point pt2(faces.info[i].face_box.x2, faces.info[i].face_box.y2);
        cv::rectangle(frame, pt1, pt2, cv::Scalar(255, 0, 0), 2);
    }

    facesdk_readModelFromFile(ModelType::Landmark, "models/face_landmark2d.bin", ImageFormat::RGB);
    sdkFaces faces2 = facesdk_landmark();
    int index = 0;
    for (int j = 0; j < faces2.face_count; j++)
    {
        index++;
        for (int i = 0; i < 424; i = i + 2)
        {
            cv::Point pt(faces2.info[j].landmarks[i], faces2.info[j].landmarks[i + 1]);
            cv::circle(frame, pt, 1, cv::Scalar(255, 0, 0), 1);
        }
        std::cout << std::endl;

        if (faces2.info[j].lefteye_close_state < eye_close_v)
        {
            std::cout << "左眼开" << std::endl;
            cv::putText(frame, "left eye open",
                        cv::Point(10, 20 + index * 30), cv::FONT_HERSHEY_DUPLEX, 1, cv::Scalar(0, 255, 255), 2);
        }
        else
        {
            std::cout << "左眼关" << std::endl;
            cv::putText(frame, "left eye close",
                        cv::Point(10, 20 + index * 30), cv::FONT_HERSHEY_DUPLEX, 1, cv::Scalar(0, 255, 255), 2);
        }

        if (faces2.info[j].righteye_close_state < eye_close_v)
        {
            std::cout << "右眼开" << std::endl;
            cv::putText(frame, "right eye open",
                        cv::Point(10, 50 + index * 30), cv::FONT_HERSHEY_DUPLEX, 1, cv::Scalar(0, 255, 255), 2);
        }
        else
        {
            std::cout << "右眼关" << std::endl;
            cv::putText(frame, "right eye close",
                        cv::Point(10, 50 + index * 30), cv::FONT_HERSHEY_DUPLEX, 1, cv::Scalar(0, 255, 255), 2);
        }
        std::cout << std::endl;
    }
    facesdk_readModelFromFile(ModelType::Attribution, "models/face_attr.bin", ImageFormat::RGB);
    sdkFaces faces3 = facesdk_attribute();
    std::cout << faces3.face_count << std::endl;
    for (int i = 0; i < faces3.face_count; i++)
    {
        std::cout << "age: " << faces3.info[i].attribution.age << std::endl;
        std::cout << "gender: " << faces3.info[i].attribution.gender << std::endl;
        std::cout << "glasses: " << faces3.info[i].attribution.glasses << std::endl;
        std::cout << "smile: " << faces3.info[i].attribution.smile << std::endl;
        std::cout << "beauty_man_look: " << faces3.info[i].attribution.beauty_man_look << std::endl;
        std::cout << "beauty_woman_look: " << faces3.info[i].attribution.beauty_woman_look << std::endl;
    }
    cv::imwrite("output/output.jpg", frame);
    facesdk_release();
}


int main(int argc, char **argv)
{
    handle_rgb(argv[1]);
    return 0;
}