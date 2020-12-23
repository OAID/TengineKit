//
// Created by zhangjun on 2020/2/12.
//

#ifndef FACE_LANDMARK_HPP
#define FACE_LANDMARK_HPP

#include <iostream>
#include <vector>
#include <memory>
#include <string.h>
#include <tengine_c_api.h>
#include <memory>
#include "data.hpp"

typedef struct FaceLandmarkInfo {
    float landmarks[212 * 2];
    float head_x;
    float head_y;
    float head_z;
    float lefteye_close_state;
    float righteye_close_state;
    float mouth_close_state;
    float mouth_bigopen_state;
} FaceLandmarkInfo;


class face_landmark {
public:
    face_landmark(std::string model_path, context_t context = nullptr,  int num_thread = 2);
    void landmark(std::vector<uint8_t*> face_list, std::vector<FaceLandmarkInfo>& landmarkOuts);

    ~face_landmark();

    int landmark_w = 112;
    int landmark_h = 112;
    int bpp = 1;

private:
    graph_t graph = nullptr;
    tensor_t input_tensor = nullptr;
    const float mean_vals[3] = {127.5, 127.5, 127.5};
    const float norm_vals[3] = {1.0 / 128.0, 1.0 / 128.0, 1.0 / 128.0};
    int num_thread;
    
};


#endif //FACE_OALLANDMARKS_HPP
