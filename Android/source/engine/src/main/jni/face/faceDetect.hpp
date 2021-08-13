#ifndef FACE_DETECT_HPP
#define FACE_DETECT_HPP

#include <string>
#include <iostream>
#include <vector>
#include <algorithm>
#include <string.h>
#include <math.h>
#include <cmath>
#include "data.hpp"
#include "utils.hpp"
#include "tenginekit_struct.hpp"


extern "C" {
#include "c_api.h"
}

//typedef struct Box {
//    float x1 = 0;
//    float y1 = 0;
//    float x2 = 0;
//    float y2 = 0;
//} Box;

//typedef struct FaceDetectInfo {
//    Box face_box;
//    float score;
//} FaceDetectInfo;

class faceDetect {
private:
    graph_t top_graph;
    tensor_t input_tensor;

    std::string scores = "scores";
    std::string boxes = "boxes";

    float score_threshold;
    float iou_threshold;
    int num_anchors;

    const float mean_vals[3] = {127, 127, 127};
    const float norm_vals[3] = {1.0 / 128, 1.0 / 128, 1.0 / 128};

    const float center_variance = 0.1;
    const float size_variance = 0.2;
    const std::vector<std::vector<float>> min_boxes = {
            {10.0f,  16.0f,  24.0f},
            {32.0f,  48.0f},
            {64.0f,  96.0f},
            {128.0f, 192.0f, 256.0f}};
    const std::vector<float> strides = {8.0, 16.0, 32.0, 64.0};
    std::vector<std::vector<float>> featuremap_size;
    std::vector<std::vector<float>> shrinkage_size;
    std::vector<int> w_h_list;

    std::vector<std::vector<float>> priors = {};
    std::vector<float> inputData;
public:
    int detectW = 160;
    int detectH = 120;
    int detectBpp = 3;
public:
    faceDetect(std::string model_path, context_t context = nullptr, int numThread = 2,
               float score_threshold_ = 0.6, float iou_threshold_ = 0.3);

    void detect(const uint8_t *img, std::vector<FaceInfo> &face_list);

    ~faceDetect();

private:
    void generateBBox(std::vector<FaceInfo> &bbox_collection, float *scores, float *boxes);

    void nms(std::vector<FaceInfo> &input, std::vector<FaceInfo> &output,
             int type = blending_nms);
};

#endif
