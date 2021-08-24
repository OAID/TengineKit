#include "utils.hpp"

double get_cur_time() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000.0 + (tv.tv_usec / 1000.0);
}


float normalize_radians(float angle) {
    return angle - 2 * M_PI * std::floor((angle - (-M_PI)) / (2 * M_PI));
}

inline float fast_exp(float x) {
    union {
        uint32_t i;
        float f;
    } v{};
    v.i = (1 << 23) * (1.4426950409f * x + 126.93490512f);
    return v.f;
}

float sigmoid(double src) {
    return (float) (1.0 / (1 + fast_exp(-src)));
}