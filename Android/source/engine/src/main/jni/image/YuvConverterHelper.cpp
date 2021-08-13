//
// Created by Hebing Shi on 2021/7/27.
//

#include <arm_neon.h>
#include "YuvConverterHelper.h"
#include <cmath>
#include <algorithm>


extern "C" {
void MNNNV21ToRGBUnit(const unsigned char *source, unsigned char *dest, size_t countDiv8,
                      const unsigned char *uv);
}


void mnnNV21RGB(const unsigned char *yin, const unsigned char *uvIn, unsigned char *dest,
                size_t count) {
    auto y = yin;
    auto uv = uvIn;
    auto dst = dest;
    int sta = 0;
#ifdef __ARM_NEON
    const int unit = 16;
    size_t countDiv8 = count / unit;
    if (countDiv8 > 0) {
        MNNNV21ToRGBUnit(y, dest, countDiv8, uv);
        sta = (int) countDiv8 * unit;
    }
#endif
    for (int i = sta; i < count; ++i) {
        int Y = y[i];
        int U = (int) uv[(i / 2) * 2 + 1] - 128;
        int V = (int) uv[(i / 2) * 2 + 0] - 128;

        Y = Y << 6;
        int R = (Y + 73 * V) >> 6;
        int G = (Y - 25 * U - 37 * V) >> 6;
        int B = (Y + 130 * U) >> 6;

        R = std::min(std::max(R, 0), 255);
        G = std::min(std::max(G, 0), 255);
        B = std::min(std::max(B, 0), 255);

        dst[3 * i + 0] = (uint8_t) R;
        dst[3 * i + 1] = (uint8_t) G;
        dst[3 * i + 2] = (uint8_t) B;
    }
}

void YuvConverterHelper::nv21RGB(const unsigned char *src, unsigned char *dst, int w, int h) {
    auto uv = src + w * h;
    int tile = h >> 1;
    for (int i = 0; i < tile; i++) {
        const unsigned char *fromY1 = src + i * 2 * w;
        const unsigned char *fromUV = uv + i * w;
        unsigned char *dst1 = dst + i * 2 * w * 3;
        unsigned char *dst2 = dst + (i * 2 + 1) * w * 3;
        auto fromY2 = src + (i * 2 + 1) * w;
        mnnNV21RGB(fromY1, fromUV, dst1, w);
        mnnNV21RGB(fromY2, fromUV, dst2, w);
    }
}
