//
// Created by Hebing Shi on 2021/7/27.
//

#include <arm_neon.h>
#include "RGBConverterHelper.h"


int RGBConverterHelper::rgba2RGB(const unsigned char *source, int w, int h,
                                 unsigned char *dst) {

    int sta = 0;
    int count = w * h;
#ifdef __ARM_NEON
    int countD8 = (int) count / 8;
    if (countD8 > 0) {
        for (int i = 0; i < countD8; ++i) {
            uint8x8x4_t rgba = vld4_u8(source + 32 * i);
            uint8x8x3_t rgb;
            rgb.val[0] = rgba.val[0];
            rgb.val[1] = rgba.val[1];
            rgb.val[2] = rgba.val[2];
            vst3_u8(dst + 24 * i, rgb);
        }
        sta = countD8 * 8;
    }
#endif
    for (int i = sta; i < count; ++i) {
        dst[3 * i + 0] = source[4 * i + 2];
        dst[3 * i + 1] = source[4 * i + 1];
        dst[3 * i + 2] = source[4 * i + 0];
    }

    return 0;
}

void RGBConverterHelper::rgb2Gray(const unsigned char *source, unsigned char *dest, size_t count) {
    int sta = 0;
#ifdef __ARM_NEON
    int countD8 = (int) count / 8;
    if (countD8 > 0) {
        auto rC = vdup_n_u8(19);
        auto gC = vdup_n_u8(38);
        auto bC = vdup_n_u8(7);
        for (int i = 0; i < countD8; ++i) {
            auto rgb = vld3_u8(source + 24 * i);
            auto res =
                    vmull_u8(rC, rgb.val[0]) + vmull_u8(gC, rgb.val[1]) + vmull_u8(bC, rgb.val[2]);
            auto resU8 = vshrn_n_u16(res, 6);
            vst1_u8(dest + 8 * i, resU8);
        }
        sta = countD8 * 8;
    }
#endif
    for (int i = sta; i < count; ++i) {
        int r = source[3 * i + 0];
        int g = source[3 * i + 1];
        int b = source[3 * i + 2];

        int y = (19 * r + 38 * g + 7 * b) >> 6;

        dest[i] = y;
    }
}
