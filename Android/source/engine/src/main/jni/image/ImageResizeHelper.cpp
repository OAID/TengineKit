//
// Created by Hebing Shi on 2021/7/26.
//

#include <android/bitmap.h>
#include <log.h>
#include <cstring>
#include <cmath>
#include <algorithm>
#include <arm_neon.h>
#include <vector>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>
#include "ImageResizeHelper.h"

void
ImageResizeHelper::resize_bilinear_c4(const unsigned char *src, int srcw, int srch,
                                      unsigned char *dst,
                                      int w, int h) {
    return ImageResizeHelper::resize_bilinear_c4(src, srcw, srch, srcw * 4, dst, w, h, w * 4);
}

void ImageResizeHelper::resize_bilinear_c1(const unsigned char *src, int srcw, int srch,
                                           unsigned char *dst, int w, int h) {
    return resize_bilinear_c1(src, srcw, srch, srcw, dst, w, h, w);
}

//void ImageResizeHelper::resize_bilinear_c2(const unsigned char *src, int srcw, int srch,
//                                           unsigned char *dst, int w, int h) {
//    return resize_bilinear_c2(src, srcw, srch, srcw * 2, dst, w, h, w * 2);
//}

void ImageResizeHelper::resize_bilinear_c3(const unsigned char *src, int srcw, int srch,
                                           unsigned char *dst, int w, int h) {
    return resize_bilinear_c3(src, srcw, srch, srcw * 3, dst, w, h, w * 3);
}

void
ImageResizeHelper::resize_bilinear_c4(const unsigned char *src, int srcw, int srch, int srcstride,
                                      unsigned char *dst,
                                      int w, int h, int stride) {
    const int INTER_RESIZE_COEF_BITS = 11;
    const int INTER_RESIZE_COEF_SCALE = 1 << INTER_RESIZE_COEF_BITS;
    //     const int ONE=INTER_RESIZE_COEF_SCALE;

    double scale_x = (double) srcw / w;
    double scale_y = (double) srch / h;

    int *buf = new int[w + h + w + h];

    int *xofs = buf;     //new int[w];
    int *yofs = buf + w; //new int[h];

    short *ialpha = (short *) (buf + w + h);    //new short[w * 2];
    short *ibeta = (short *) (buf + w + h + w); //new short[h * 2];

    float fx;
    float fy;
    int sx;
    int sy;

#define SATURATE_CAST_SHORT(X) (short)::std::min(::std::max((int)(X + (X >= 0.f ? 0.5f : -0.5f)), SHRT_MIN), SHRT_MAX);

    for (int dx = 0; dx < w; dx++) {
        fx = (float) ((dx + 0.5) * scale_x - 0.5);
        sx = static_cast<int>(floor(fx));
        fx -= sx;

        if (sx < 0) {
            sx = 0;
            fx = 0.f;
        }
        if (sx >= srcw - 1) {
            sx = srcw - 2;
            fx = 1.f;
        }

        xofs[dx] = sx * 4;

        float a0 = (1.f - fx) * INTER_RESIZE_COEF_SCALE;
        float a1 = fx * INTER_RESIZE_COEF_SCALE;

        ialpha[dx * 2] = SATURATE_CAST_SHORT(a0);
        ialpha[dx * 2 + 1] = SATURATE_CAST_SHORT(a1);
    }

    for (int dy = 0; dy < h; dy++) {
        fy = (float) ((dy + 0.5) * scale_y - 0.5);
        sy = static_cast<int>(floor(fy));
        fy -= sy;

        if (sy < 0) {
            sy = 0;
            fy = 0.f;
        }
        if (sy >= srch - 1) {
            sy = srch - 2;
            fy = 1.f;
        }

        yofs[dy] = sy;

        float b0 = (1.f - fy) * INTER_RESIZE_COEF_SCALE;
        float b1 = fy * INTER_RESIZE_COEF_SCALE;

        ibeta[dy * 2] = SATURATE_CAST_SHORT(b0);
        ibeta[dy * 2 + 1] = SATURATE_CAST_SHORT(b1);
    }

#undef SATURATE_CAST_SHORT

    // loop body
//    Mat rowsbuf0(w * 4, (size_t) 2u);
//    Mat rowsbuf1(w * 4, (size_t) 2u);
    std::vector<uint8_t> rowsbuf0(w * 4 * 2);
    std::vector<uint8_t> rowsbuf1(w * 4 * 2);

    short *rows0 = (short *) rowsbuf0.data();
    short *rows1 = (short *) rowsbuf1.data();

    int prev_sy1 = -2;

    for (int dy = 0; dy < h; dy++) {
        sy = yofs[dy];

        if (sy == prev_sy1) {
            // reuse all rows
        } else if (sy == prev_sy1 + 1) {
            // hresize one row
            short *rows0_old = rows0;
            rows0 = rows1;
            rows1 = rows0_old;
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0 = vdup_n_s16(a0);
                int16x4_t _a1 = vdup_n_s16(a1);
                uint8x8_t _S1 = vld1_u8(S1p);
                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S1low = vget_low_s16(_S116);
                int16x4_t _S1high = vget_high_s16(_S116);
                int32x4_t _rows1 = vmull_s16(_S1low, _a0);
                _rows1 = vmlal_s16(_rows1, _S1high, _a1);
                int16x4_t _rows1_sr4 = vshrn_n_s32(_rows1, 4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                rows1p[0] = (S1p[0] * a0 + S1p[4] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[5] * a1) >> 4;
                rows1p[2] = (S1p[2] * a0 + S1p[6] * a1) >> 4;
                rows1p[3] = (S1p[3] * a0 + S1p[7] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows1p += 4;
            }
        } else {
            // hresize two rows
            const unsigned char *S0 = src + srcstride * (sy);
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows0p = rows0;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S0p = S0 + sx;
                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0 = vdup_n_s16(a0);
                int16x4_t _a1 = vdup_n_s16(a1);
                uint8x8_t _S0 = vld1_u8(S0p);
                uint8x8_t _S1 = vld1_u8(S1p);
                int16x8_t _S016 = vreinterpretq_s16_u16(vmovl_u8(_S0));
                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S0low = vget_low_s16(_S016);
                int16x4_t _S1low = vget_low_s16(_S116);
                int16x4_t _S0high = vget_high_s16(_S016);
                int16x4_t _S1high = vget_high_s16(_S116);
                int32x4_t _rows0 = vmull_s16(_S0low, _a0);
                int32x4_t _rows1 = vmull_s16(_S1low, _a0);
                _rows0 = vmlal_s16(_rows0, _S0high, _a1);
                _rows1 = vmlal_s16(_rows1, _S1high, _a1);
                int16x4_t _rows0_sr4 = vshrn_n_s32(_rows0, 4);
                int16x4_t _rows1_sr4 = vshrn_n_s32(_rows1, 4);
                vst1_s16(rows0p, _rows0_sr4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                rows0p[0] = (S0p[0] * a0 + S0p[4] * a1) >> 4;
                rows0p[1] = (S0p[1] * a0 + S0p[5] * a1) >> 4;
                rows0p[2] = (S0p[2] * a0 + S0p[6] * a1) >> 4;
                rows0p[3] = (S0p[3] * a0 + S0p[7] * a1) >> 4;
                rows1p[0] = (S1p[0] * a0 + S1p[4] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[5] * a1) >> 4;
                rows1p[2] = (S1p[2] * a0 + S1p[6] * a1) >> 4;
                rows1p[3] = (S1p[3] * a0 + S1p[7] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows0p += 4;
                rows1p += 4;
            }
        }

        prev_sy1 = sy;

        // vresize
        short b0 = ibeta[0];
        short b1 = ibeta[1];

        short *rows0p = rows0;
        short *rows1p = rows1;
        unsigned char *Dp = dst + stride * (dy);

#if __ARM_NEON
        int nn = (w * 4) >> 3;
#else
        int nn = 0;
#endif
        int remain = (w * 4) - (nn << 3);

#if __ARM_NEON
#if __aarch64__
        int16x4_t _b0 = vdup_n_s16(b0);
        int16x4_t _b1 = vdup_n_s16(b1);
        int32x4_t _v2 = vdupq_n_s32(2);
        for (; nn > 0; nn--) {
            int16x4_t _rows0p_sr4 = vld1_s16(rows0p);
            int16x4_t _rows1p_sr4 = vld1_s16(rows1p);
            int16x4_t _rows0p_1_sr4 = vld1_s16(rows0p + 4);
            int16x4_t _rows1p_1_sr4 = vld1_s16(rows1p + 4);

            int32x4_t _rows0p_sr4_mb0 = vmull_s16(_rows0p_sr4, _b0);
            int32x4_t _rows1p_sr4_mb1 = vmull_s16(_rows1p_sr4, _b1);
            int32x4_t _rows0p_1_sr4_mb0 = vmull_s16(_rows0p_1_sr4, _b0);
            int32x4_t _rows1p_1_sr4_mb1 = vmull_s16(_rows1p_1_sr4, _b1);

            int32x4_t _acc = _v2;
            _acc = vsraq_n_s32(_acc, _rows0p_sr4_mb0, 16);
            _acc = vsraq_n_s32(_acc, _rows1p_sr4_mb1, 16);

            int32x4_t _acc_1 = _v2;
            _acc_1 = vsraq_n_s32(_acc_1, _rows0p_1_sr4_mb0, 16);
            _acc_1 = vsraq_n_s32(_acc_1, _rows1p_1_sr4_mb1, 16);

            int16x4_t _acc16 = vshrn_n_s32(_acc, 2);
            int16x4_t _acc16_1 = vshrn_n_s32(_acc_1, 2);

            uint8x8_t _D = vqmovun_s16(vcombine_s16(_acc16, _acc16_1));

            vst1_u8(Dp, _D);

            Dp += 8;
            rows0p += 8;
            rows1p += 8;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "vdup.s16   d16, %8         \n"
                "mov        r4, #2          \n"
                "vdup.s16   d17, %9         \n"
                "vdup.s32   q12, r4         \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "0:                         \n"
                "vmull.s16  q0, d2, d16     \n"
                "vmull.s16  q1, d3, d16     \n"
                "vorr.s32   q10, q12, q12   \n"
                "vorr.s32   q11, q12, q12   \n"
                "vmull.s16  q2, d6, d17     \n"
                "vmull.s16  q3, d7, d17     \n"
                "vsra.s32   q10, q0, #16    \n"
                "vsra.s32   q11, q1, #16    \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "vsra.s32   q10, q2, #16    \n"
                "vsra.s32   q11, q3, #16    \n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "vshrn.s32  d20, q10, #2    \n"
                "vshrn.s32  d21, q11, #2    \n"
                "vqmovun.s16 d20, q10        \n"
                "vst1.8     {d20}, [%2]!    \n"
                "subs       %3, #1          \n"
                "bne        0b              \n"
                "sub        %0, #16         \n"
                "sub        %1, #16         \n"
                : "=r"(rows0p), // %0
                "=r"(rows1p), // %1
                "=r"(Dp),     // %2
                "=r"(nn)      // %3
                : "0"(rows0p),
                "1"(rows1p),
                "2"(Dp),
                "3"(nn),
                "r"(b0), // %8
                "r"(b1)  // %9
                : "cc", "memory", "r4", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11", "q12");
        }
#endif // __aarch64__
#endif // __ARM_NEON
        for (; remain; --remain) {
            //             D[x] = (rows0[x]*b0 + rows1[x]*b1) >> INTER_RESIZE_COEF_BITS;
            *Dp++ = (unsigned char) (((short) ((b0 * (short) (*rows0p++)) >> 16) +
                                      (short) ((b1 * (short) (*rows1p++)) >> 16) + 2) >> 2);
        }

        ibeta += 2;
    }

    delete[] buf;
}


void
ImageResizeHelper::resize_bilinear_c1(const unsigned char *src, int srcw, int srch, int srcstride,
                                      unsigned char *dst, int w, int h, int stride) {
    const int INTER_RESIZE_COEF_BITS = 11;
    const int INTER_RESIZE_COEF_SCALE = 1 << INTER_RESIZE_COEF_BITS;
    //     const int ONE=INTER_RESIZE_COEF_SCALE;

    double scale_x = (double) srcw / w;
    double scale_y = (double) srch / h;

    int *buf = new int[w + h + w + h];

    int *xofs = buf;     //new int[w];
    int *yofs = buf + w; //new int[h];

    short *ialpha = (short *) (buf + w + h);    //new short[w * 2];
    short *ibeta = (short *) (buf + w + h + w); //new short[h * 2];

    float fx;
    float fy;
    int sx;
    int sy;

#define SATURATE_CAST_SHORT(X) (short)::std::min(::std::max((int)(X + (X >= 0.f ? 0.5f : -0.5f)), SHRT_MIN), SHRT_MAX);

    for (int dx = 0; dx < w; dx++) {
        fx = (float) ((dx + 0.5) * scale_x - 0.5);
        sx = static_cast<int>(floor(fx));
        fx -= sx;

        if (sx < 0) {
            sx = 0;
            fx = 0.f;
        }
        if (sx >= srcw - 1) {
            sx = srcw - 2;
            fx = 1.f;
        }

        xofs[dx] = sx;

        float a0 = (1.f - fx) * INTER_RESIZE_COEF_SCALE;
        float a1 = fx * INTER_RESIZE_COEF_SCALE;

        ialpha[dx * 2] = SATURATE_CAST_SHORT(a0);
        ialpha[dx * 2 + 1] = SATURATE_CAST_SHORT(a1);
    }

    for (int dy = 0; dy < h; dy++) {
        fy = (float) ((dy + 0.5) * scale_y - 0.5);
        sy = static_cast<int>(floor(fy));
        fy -= sy;

        if (sy < 0) {
            sy = 0;
            fy = 0.f;
        }
        if (sy >= srch - 1) {
            sy = srch - 2;
            fy = 1.f;
        }

        yofs[dy] = sy;

        float b0 = (1.f - fy) * INTER_RESIZE_COEF_SCALE;
        float b1 = fy * INTER_RESIZE_COEF_SCALE;

        ibeta[dy * 2] = SATURATE_CAST_SHORT(b0);
        ibeta[dy * 2 + 1] = SATURATE_CAST_SHORT(b1);
    }

#undef SATURATE_CAST_SHORT

    // loop body
//    Mat rowsbuf0(w, (size_t)2u);
//    Mat rowsbuf1(w, (size_t)2u);
    std::vector<uint8_t> rowsbuf0(w * 2);
    std::vector<uint8_t> rowsbuf1(w * 2);
    short *rows0 = (short *) rowsbuf0.data();
    short *rows1 = (short *) rowsbuf1.data();

    int prev_sy1 = -2;

    for (int dy = 0; dy < h; dy++) {
        sy = yofs[dy];

        if (sy == prev_sy1) {
            // reuse all rows
        } else if (sy == prev_sy1 + 1) {
            // hresize one row
            short *rows0_old = rows0;
            rows0 = rows1;
            rows1 = rows0_old;
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S1p = S1 + sx;
                rows1p[dx] = (S1p[0] * a0 + S1p[1] * a1) >> 4;

                ialphap += 2;
            }
        } else {
            // hresize two rows
            const unsigned char *S0 = src + srcstride * (sy);
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows0p = rows0;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S0p = S0 + sx;
                const unsigned char *S1p = S1 + sx;
                rows0p[dx] = (S0p[0] * a0 + S0p[1] * a1) >> 4;
                rows1p[dx] = (S1p[0] * a0 + S1p[1] * a1) >> 4;

                ialphap += 2;
            }
        }

        prev_sy1 = sy;

        // vresize
        short b0 = ibeta[0];
        short b1 = ibeta[1];

        short *rows0p = rows0;
        short *rows1p = rows1;
        unsigned char *Dp = dst + stride * (dy);

#if __ARM_NEON
        int nn = w >> 3;
#else
        int nn = 0;
#endif
        int remain = w - (nn << 3);

#if __ARM_NEON
#if __aarch64__
        int16x4_t _b0 = vdup_n_s16(b0);
        int16x4_t _b1 = vdup_n_s16(b1);
        int32x4_t _v2 = vdupq_n_s32(2);
        for (; nn > 0; nn--) {
            int16x4_t _rows0p_sr4 = vld1_s16(rows0p);
            int16x4_t _rows1p_sr4 = vld1_s16(rows1p);
            int16x4_t _rows0p_1_sr4 = vld1_s16(rows0p + 4);
            int16x4_t _rows1p_1_sr4 = vld1_s16(rows1p + 4);

            int32x4_t _rows0p_sr4_mb0 = vmull_s16(_rows0p_sr4, _b0);
            int32x4_t _rows1p_sr4_mb1 = vmull_s16(_rows1p_sr4, _b1);
            int32x4_t _rows0p_1_sr4_mb0 = vmull_s16(_rows0p_1_sr4, _b0);
            int32x4_t _rows1p_1_sr4_mb1 = vmull_s16(_rows1p_1_sr4, _b1);

            int32x4_t _acc = _v2;
            _acc = vsraq_n_s32(_acc, _rows0p_sr4_mb0, 16);
            _acc = vsraq_n_s32(_acc, _rows1p_sr4_mb1, 16);

            int32x4_t _acc_1 = _v2;
            _acc_1 = vsraq_n_s32(_acc_1, _rows0p_1_sr4_mb0, 16);
            _acc_1 = vsraq_n_s32(_acc_1, _rows1p_1_sr4_mb1, 16);

            int16x4_t _acc16 = vshrn_n_s32(_acc, 2);
            int16x4_t _acc16_1 = vshrn_n_s32(_acc_1, 2);

            uint8x8_t _D = vqmovun_s16(vcombine_s16(_acc16, _acc16_1));

            vst1_u8(Dp, _D);

            Dp += 8;
            rows0p += 8;
            rows1p += 8;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "vdup.s16   d16, %8         \n"
                "mov        r4, #2          \n"
                "vdup.s16   d17, %9         \n"
                "vdup.s32   q12, r4         \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "0:                         \n"
                "vmull.s16  q0, d2, d16     \n"
                "vmull.s16  q1, d3, d16     \n"
                "vorr.s32   q10, q12, q12   \n"
                "vorr.s32   q11, q12, q12   \n"
                "vmull.s16  q2, d6, d17     \n"
                "vmull.s16  q3, d7, d17     \n"
                "vsra.s32   q10, q0, #16    \n"
                "vsra.s32   q11, q1, #16    \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "vsra.s32   q10, q2, #16    \n"
                "vsra.s32   q11, q3, #16    \n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "vshrn.s32  d20, q10, #2    \n"
                "vshrn.s32  d21, q11, #2    \n"
                "vqmovun.s16 d20, q10        \n"
                "vst1.8     {d20}, [%2]!    \n"
                "subs       %3, #1          \n"
                "bne        0b              \n"
                "sub        %0, #16         \n"
                "sub        %1, #16         \n"
                : "=r"(rows0p), // %0
                "=r"(rows1p), // %1
                "=r"(Dp),     // %2
                "=r"(nn)      // %3
                : "0"(rows0p),
                "1"(rows1p),
                "2"(Dp),
                "3"(nn),
                "r"(b0), // %8
                "r"(b1)  // %9
                : "cc", "memory", "r4", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11", "q12");
        }
#endif // __aarch64__
#endif // __ARM_NEON
        for (; remain; --remain) {
            //             D[x] = (rows0[x]*b0 + rows1[x]*b1) >> INTER_RESIZE_COEF_BITS;
            *Dp++ = (unsigned char) (((short) ((b0 * (short) (*rows0p++)) >> 16) +
                                      (short) ((b1 * (short) (*rows1p++)) >> 16) + 2) >> 2);
        }

        ibeta += 2;
    }

    delete[] buf;
}


void
ImageResizeHelper::resize_bilinear_c3(const unsigned char *src, int srcw, int srch, int srcstride,
                                      unsigned char *dst,
                                      int w, int h, int stride) {
    const int INTER_RESIZE_COEF_BITS = 11;
    const int INTER_RESIZE_COEF_SCALE = 1 << INTER_RESIZE_COEF_BITS;
    //     const int ONE=INTER_RESIZE_COEF_SCALE;

    double scale_x = (double) srcw / w;
    double scale_y = (double) srch / h;

    int *buf = new int[w + h + w + h];

    int *xofs = buf;     //new int[w];
    int *yofs = buf + w; //new int[h];

    short *ialpha = (short *) (buf + w + h);    //new short[w * 2];
    short *ibeta = (short *) (buf + w + h + w); //new short[h * 2];

    float fx;
    float fy;
    int sx;
    int sy;

#define SATURATE_CAST_SHORT(X) (short)::std::min(::std::max((int)(X + (X >= 0.f ? 0.5f : -0.5f)), SHRT_MIN), SHRT_MAX);

    for (int dx = 0; dx < w; dx++) {
        fx = (float) ((dx + 0.5) * scale_x - 0.5);
        sx = static_cast<int>(floor(fx));
        fx -= sx;

        if (sx < 0) {
            sx = 0;
            fx = 0.f;
        }
        if (sx >= srcw - 1) {
            sx = srcw - 2;
            fx = 1.f;
        }

        xofs[dx] = sx * 3;

        float a0 = (1.f - fx) * INTER_RESIZE_COEF_SCALE;
        float a1 = fx * INTER_RESIZE_COEF_SCALE;

        ialpha[dx * 2] = SATURATE_CAST_SHORT(a0);
        ialpha[dx * 2 + 1] = SATURATE_CAST_SHORT(a1);
    }

    for (int dy = 0; dy < h; dy++) {
        fy = (float) ((dy + 0.5) * scale_y - 0.5);
        sy = static_cast<int>(floor(fy));
        fy -= sy;

        if (sy < 0) {
            sy = 0;
            fy = 0.f;
        }
        if (sy >= srch - 1) {
            sy = srch - 2;
            fy = 1.f;
        }

        yofs[dy] = sy;

        float b0 = (1.f - fy) * INTER_RESIZE_COEF_SCALE;
        float b1 = fy * INTER_RESIZE_COEF_SCALE;

        ibeta[dy * 2] = SATURATE_CAST_SHORT(b0);
        ibeta[dy * 2 + 1] = SATURATE_CAST_SHORT(b1);
    }

#undef SATURATE_CAST_SHORT

    // loop body
//    Mat rowsbuf0(w * 3 + 1, (size_t)2u);
//    Mat rowsbuf1(w * 3 + 1, (size_t)2u);
    std::vector<uint8_t> rowsbuf0((w * 3 + 1) * 2);
    std::vector<uint8_t> rowsbuf1((w * 3 + 1) * 2);


    short *rows0 = (short *) rowsbuf0.data();
    short *rows1 = (short *) rowsbuf1.data();

    int prev_sy1 = -2;

    for (int dy = 0; dy < h; dy++) {
        sy = yofs[dy];

        if (sy == prev_sy1) {
            // reuse all rows
        } else if (sy == prev_sy1 + 1) {
            // hresize one row
            short *rows0_old = rows0;
            rows0 = rows1;
            rows1 = rows0_old;
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0 = vdup_n_s16(a0);
                int16x4_t _a1 = vdup_n_s16(a1);
                uint8x8_t _S1 = uint8x8_t();

                _S1 = vld1_lane_u8(S1p, _S1, 0);
                _S1 = vld1_lane_u8(S1p + 1, _S1, 1);
                _S1 = vld1_lane_u8(S1p + 2, _S1, 2);
                _S1 = vld1_lane_u8(S1p + 3, _S1, 3);
                _S1 = vld1_lane_u8(S1p + 4, _S1, 4);
                _S1 = vld1_lane_u8(S1p + 5, _S1, 5);

                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S1low = vget_low_s16(_S116);
                int16x4_t _S1high = vext_s16(_S1low, vget_high_s16(_S116), 3);
                int32x4_t _rows1 = vmull_s16(_S1low, _a0);
                _rows1 = vmlal_s16(_rows1, _S1high, _a1);
                int16x4_t _rows1_sr4 = vshrn_n_s32(_rows1, 4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                rows1p[0] = (S1p[0] * a0 + S1p[3] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[4] * a1) >> 4;
                rows1p[2] = (S1p[2] * a0 + S1p[5] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows1p += 3;
            }
        } else {
            // hresize two rows
            const unsigned char *S0 = src + srcstride * (sy);
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows0p = rows0;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S0p = S0 + sx;
                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0 = vdup_n_s16(a0);
                int16x4_t _a1 = vdup_n_s16(a1);
                uint8x8_t _S0 = uint8x8_t();
                uint8x8_t _S1 = uint8x8_t();

                _S0 = vld1_lane_u8(S0p, _S0, 0);
                _S0 = vld1_lane_u8(S0p + 1, _S0, 1);
                _S0 = vld1_lane_u8(S0p + 2, _S0, 2);
                _S0 = vld1_lane_u8(S0p + 3, _S0, 3);
                _S0 = vld1_lane_u8(S0p + 4, _S0, 4);
                _S0 = vld1_lane_u8(S0p + 5, _S0, 5);

                _S1 = vld1_lane_u8(S1p, _S1, 0);
                _S1 = vld1_lane_u8(S1p + 1, _S1, 1);
                _S1 = vld1_lane_u8(S1p + 2, _S1, 2);
                _S1 = vld1_lane_u8(S1p + 3, _S1, 3);
                _S1 = vld1_lane_u8(S1p + 4, _S1, 4);
                _S1 = vld1_lane_u8(S1p + 5, _S1, 5);

                int16x8_t _S016 = vreinterpretq_s16_u16(vmovl_u8(_S0));
                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S0low = vget_low_s16(_S016);
                int16x4_t _S1low = vget_low_s16(_S116);
                int16x4_t _S0high = vext_s16(_S0low, vget_high_s16(_S016), 3);
                int16x4_t _S1high = vext_s16(_S1low, vget_high_s16(_S116), 3);
                int32x4_t _rows0 = vmull_s16(_S0low, _a0);
                int32x4_t _rows1 = vmull_s16(_S1low, _a0);
                _rows0 = vmlal_s16(_rows0, _S0high, _a1);
                _rows1 = vmlal_s16(_rows1, _S1high, _a1);
                int16x4_t _rows0_sr4 = vshrn_n_s32(_rows0, 4);
                int16x4_t _rows1_sr4 = vshrn_n_s32(_rows1, 4);
                vst1_s16(rows0p, _rows0_sr4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                rows0p[0] = (S0p[0] * a0 + S0p[3] * a1) >> 4;
                rows0p[1] = (S0p[1] * a0 + S0p[4] * a1) >> 4;
                rows0p[2] = (S0p[2] * a0 + S0p[5] * a1) >> 4;
                rows1p[0] = (S1p[0] * a0 + S1p[3] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[4] * a1) >> 4;
                rows1p[2] = (S1p[2] * a0 + S1p[5] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows0p += 3;
                rows1p += 3;
            }
        }

        prev_sy1 = sy;

        // vresize
        short b0 = ibeta[0];
        short b1 = ibeta[1];

        short *rows0p = rows0;
        short *rows1p = rows1;
        unsigned char *Dp = dst + stride * (dy);

#if __ARM_NEON
        int nn = (w * 3) >> 3;
#else
        int nn = 0;
#endif
        int remain = (w * 3) - (nn << 3);

#if __ARM_NEON
#if __aarch64__
        int16x4_t _b0 = vdup_n_s16(b0);
        int16x4_t _b1 = vdup_n_s16(b1);
        int32x4_t _v2 = vdupq_n_s32(2);
        for (; nn > 0; nn--) {
            int16x4_t _rows0p_sr4 = vld1_s16(rows0p);
            int16x4_t _rows1p_sr4 = vld1_s16(rows1p);
            int16x4_t _rows0p_1_sr4 = vld1_s16(rows0p + 4);
            int16x4_t _rows1p_1_sr4 = vld1_s16(rows1p + 4);

            int32x4_t _rows0p_sr4_mb0 = vmull_s16(_rows0p_sr4, _b0);
            int32x4_t _rows1p_sr4_mb1 = vmull_s16(_rows1p_sr4, _b1);
            int32x4_t _rows0p_1_sr4_mb0 = vmull_s16(_rows0p_1_sr4, _b0);
            int32x4_t _rows1p_1_sr4_mb1 = vmull_s16(_rows1p_1_sr4, _b1);

            int32x4_t _acc = _v2;
            _acc = vsraq_n_s32(_acc, _rows0p_sr4_mb0, 16);
            _acc = vsraq_n_s32(_acc, _rows1p_sr4_mb1, 16);

            int32x4_t _acc_1 = _v2;
            _acc_1 = vsraq_n_s32(_acc_1, _rows0p_1_sr4_mb0, 16);
            _acc_1 = vsraq_n_s32(_acc_1, _rows1p_1_sr4_mb1, 16);

            int16x4_t _acc16 = vshrn_n_s32(_acc, 2);
            int16x4_t _acc16_1 = vshrn_n_s32(_acc_1, 2);

            uint8x8_t _D = vqmovun_s16(vcombine_s16(_acc16, _acc16_1));

            vst1_u8(Dp, _D);

            Dp += 8;
            rows0p += 8;
            rows1p += 8;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "vdup.s16   d16, %8         \n"
                "mov        r4, #2          \n"
                "vdup.s16   d17, %9         \n"
                "vdup.s32   q12, r4         \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "0:                         \n"
                "vmull.s16  q0, d2, d16     \n"
                "vmull.s16  q1, d3, d16     \n"
                "vorr.s32   q10, q12, q12   \n"
                "vorr.s32   q11, q12, q12   \n"
                "vmull.s16  q2, d6, d17     \n"
                "vmull.s16  q3, d7, d17     \n"
                "vsra.s32   q10, q0, #16    \n"
                "vsra.s32   q11, q1, #16    \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "vsra.s32   q10, q2, #16    \n"
                "vsra.s32   q11, q3, #16    \n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "vshrn.s32  d20, q10, #2    \n"
                "vshrn.s32  d21, q11, #2    \n"
                "vqmovun.s16 d20, q10        \n"
                "vst1.8     {d20}, [%2]!    \n"
                "subs       %3, #1          \n"
                "bne        0b              \n"
                "sub        %0, #16         \n"
                "sub        %1, #16         \n"
                : "=r"(rows0p), // %0
                "=r"(rows1p), // %1
                "=r"(Dp),     // %2
                "=r"(nn)      // %3
                : "0"(rows0p),
                "1"(rows1p),
                "2"(Dp),
                "3"(nn),
                "r"(b0), // %8
                "r"(b1)  // %9
                : "cc", "memory", "r4", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11", "q12");
        }
#endif // __aarch64__
#endif // __ARM_NEON
        for (; remain; --remain) {
            //             D[x] = (rows0[x]*b0 + rows1[x]*b1) >> INTER_RESIZE_COEF_BITS;
            *Dp++ = (unsigned char) (((short) ((b0 * (short) (*rows0p++)) >> 16) +
                                      (short) ((b1 * (short) (*rows1p++)) >> 16) + 2) >> 2);
        }

        ibeta += 2;
    }

    delete[] buf;
}

void
ImageResizeHelper::resize_bilinear_c2(const unsigned char *src, int srcw, int srch, int srcstride,
                                      unsigned char *dst, int w, int h, int stride) {
    const int INTER_RESIZE_COEF_BITS = 11;
    const int INTER_RESIZE_COEF_SCALE = 1 << INTER_RESIZE_COEF_BITS;
    //     const int ONE=INTER_RESIZE_COEF_SCALE;

    double scale_x = (double) srcw / w;
    double scale_y = (double) srch / h;

    int *buf = new int[w + h + w + h];

    int *xofs = buf;     //new int[w];
    int *yofs = buf + w; //new int[h];

    short *ialpha = (short *) (buf + w + h);    //new short[w * 2];
    short *ibeta = (short *) (buf + w + h + w); //new short[h * 2];

    float fx;
    float fy;
    int sx;
    int sy;

#define SATURATE_CAST_SHORT(X) (short)::std::min(::std::max((int)(X + (X >= 0.f ? 0.5f : -0.5f)), SHRT_MIN), SHRT_MAX);

    for (int dx = 0; dx < w; dx++) {
        fx = (float) ((dx + 0.5) * scale_x - 0.5);
        sx = static_cast<int>(floor(fx));
        fx -= sx;

        if (sx < 0) {
            sx = 0;
            fx = 0.f;
        }
        if (sx >= srcw - 1) {
            sx = srcw - 2;
            fx = 1.f;
        }

        xofs[dx] = sx * 2;

        float a0 = (1.f - fx) * INTER_RESIZE_COEF_SCALE;
        float a1 = fx * INTER_RESIZE_COEF_SCALE;

        ialpha[dx * 2] = SATURATE_CAST_SHORT(a0);
        ialpha[dx * 2 + 1] = SATURATE_CAST_SHORT(a1);
    }

    for (int dy = 0; dy < h; dy++) {
        fy = (float) ((dy + 0.5) * scale_y - 0.5);
        sy = static_cast<int>(floor(fy));
        fy -= sy;

        if (sy < 0) {
            sy = 0;
            fy = 0.f;
        }
        if (sy >= srch - 1) {
            sy = srch - 2;
            fy = 1.f;
        }

        yofs[dy] = sy;

        float b0 = (1.f - fy) * INTER_RESIZE_COEF_SCALE;
        float b1 = fy * INTER_RESIZE_COEF_SCALE;

        ibeta[dy * 2] = SATURATE_CAST_SHORT(b0);
        ibeta[dy * 2 + 1] = SATURATE_CAST_SHORT(b1);
    }

#undef SATURATE_CAST_SHORT

    // loop body
//    Mat rowsbuf0(w * 2 + 2, (size_t)2u);
//    Mat rowsbuf1(w * 2 + 2, (size_t)2u);
    std::vector<uint8_t> rowsbuf0((w * 2 + 2) * 2);
    std::vector<uint8_t> rowsbuf1((w * 2 + 2) * 2);
    short *rows0 = (short *) rowsbuf0.data();
    short *rows1 = (short *) rowsbuf1.data();

    int prev_sy1 = -2;

    for (int dy = 0; dy < h; dy++) {
        sy = yofs[dy];

        if (sy == prev_sy1) {
            // reuse all rows
        } else if (sy == prev_sy1 + 1) {
            // hresize one row
            short *rows0_old = rows0;
            rows0 = rows1;
            rows1 = rows0_old;
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];

                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0a1XX = vld1_s16(ialphap);
                int16x4_t _a0a0a1a1 = vzip_s16(_a0a1XX, _a0a1XX).val[0];
                uint8x8_t _S1 = uint8x8_t();

                _S1 = vld1_lane_u8(S1p, _S1, 0);
                _S1 = vld1_lane_u8(S1p + 1, _S1, 1);
                _S1 = vld1_lane_u8(S1p + 2, _S1, 2);
                _S1 = vld1_lane_u8(S1p + 3, _S1, 3);

                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S1lowhigh = vget_low_s16(_S116);
                int32x4_t _S1ma0a1 = vmull_s16(_S1lowhigh, _a0a0a1a1);
                int32x2_t _rows1low = vadd_s32(vget_low_s32(_S1ma0a1), vget_high_s32(_S1ma0a1));
                int32x4_t _rows1 = vcombine_s32(_rows1low, vget_high_s32(_S1ma0a1));
                int16x4_t _rows1_sr4 = vshrn_n_s32(_rows1, 4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                rows1p[0] = (S1p[0] * a0 + S1p[2] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[3] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows1p += 2;
            }
        } else {
            // hresize two rows
            const unsigned char *S0 = src + srcstride * (sy);
            const unsigned char *S1 = src + srcstride * (sy + 1);

            const short *ialphap = ialpha;
            short *rows0p = rows0;
            short *rows1p = rows1;
            for (int dx = 0; dx < w; dx++) {
                sx = xofs[dx];
                short a0 = ialphap[0];
                short a1 = ialphap[1];

                const unsigned char *S0p = S0 + sx;
                const unsigned char *S1p = S1 + sx;
#if __ARM_NEON
                int16x4_t _a0 = vdup_n_s16(a0);
                int16x4_t _a1 = vdup_n_s16(a1);
                uint8x8_t _S0 = uint8x8_t();
                uint8x8_t _S1 = uint8x8_t();

                _S0 = vld1_lane_u8(S0p, _S0, 0);
                _S0 = vld1_lane_u8(S0p + 1, _S0, 1);
                _S0 = vld1_lane_u8(S0p + 2, _S0, 2);
                _S0 = vld1_lane_u8(S0p + 3, _S0, 3);

                _S1 = vld1_lane_u8(S1p, _S1, 0);
                _S1 = vld1_lane_u8(S1p + 1, _S1, 1);
                _S1 = vld1_lane_u8(S1p + 2, _S1, 2);
                _S1 = vld1_lane_u8(S1p + 3, _S1, 3);

                int16x8_t _S016 = vreinterpretq_s16_u16(vmovl_u8(_S0));
                int16x8_t _S116 = vreinterpretq_s16_u16(vmovl_u8(_S1));
                int16x4_t _S0lowhigh = vget_low_s16(_S016);
                int16x4_t _S1lowhigh = vget_low_s16(_S116);
                int32x2x2_t _S0S1low_S0S1high = vtrn_s32(vreinterpret_s32_s16(_S0lowhigh),
                                                         vreinterpret_s32_s16(_S1lowhigh));
                int32x4_t _rows01 = vmull_s16(vreinterpret_s16_s32(_S0S1low_S0S1high.val[0]), _a0);
                _rows01 = vmlal_s16(_rows01, vreinterpret_s16_s32(_S0S1low_S0S1high.val[1]), _a1);
                int16x4_t _rows01_sr4 = vshrn_n_s32(_rows01, 4);
                int16x4_t _rows1_sr4 = vext_s16(_rows01_sr4, _rows01_sr4, 2);
                vst1_s16(rows0p, _rows01_sr4);
                vst1_s16(rows1p, _rows1_sr4);
#else
                rows0p[0] = (S0p[0] * a0 + S0p[2] * a1) >> 4;
                rows0p[1] = (S0p[1] * a0 + S0p[3] * a1) >> 4;
                rows1p[0] = (S1p[0] * a0 + S1p[2] * a1) >> 4;
                rows1p[1] = (S1p[1] * a0 + S1p[3] * a1) >> 4;
#endif // __ARM_NEON

                ialphap += 2;
                rows0p += 2;
                rows1p += 2;
            }
        }

        prev_sy1 = sy;

        // vresize
        short b0 = ibeta[0];
        short b1 = ibeta[1];

        short *rows0p = rows0;
        short *rows1p = rows1;
        unsigned char *Dp = dst + stride * (dy);

#if __ARM_NEON
        int nn = (w * 2) >> 3;
#else
        int nn = 0;
#endif
        int remain = (w * 2) - (nn << 3);

#if __ARM_NEON
#if __aarch64__
        int16x4_t _b0 = vdup_n_s16(b0);
        int16x4_t _b1 = vdup_n_s16(b1);
        int32x4_t _v2 = vdupq_n_s32(2);
        for (; nn > 0; nn--) {
            int16x4_t _rows0p_sr4 = vld1_s16(rows0p);
            int16x4_t _rows1p_sr4 = vld1_s16(rows1p);
            int16x4_t _rows0p_1_sr4 = vld1_s16(rows0p + 4);
            int16x4_t _rows1p_1_sr4 = vld1_s16(rows1p + 4);

            int32x4_t _rows0p_sr4_mb0 = vmull_s16(_rows0p_sr4, _b0);
            int32x4_t _rows1p_sr4_mb1 = vmull_s16(_rows1p_sr4, _b1);
            int32x4_t _rows0p_1_sr4_mb0 = vmull_s16(_rows0p_1_sr4, _b0);
            int32x4_t _rows1p_1_sr4_mb1 = vmull_s16(_rows1p_1_sr4, _b1);

            int32x4_t _acc = _v2;
            _acc = vsraq_n_s32(_acc, _rows0p_sr4_mb0, 16);
            _acc = vsraq_n_s32(_acc, _rows1p_sr4_mb1, 16);

            int32x4_t _acc_1 = _v2;
            _acc_1 = vsraq_n_s32(_acc_1, _rows0p_1_sr4_mb0, 16);
            _acc_1 = vsraq_n_s32(_acc_1, _rows1p_1_sr4_mb1, 16);

            int16x4_t _acc16 = vshrn_n_s32(_acc, 2);
            int16x4_t _acc16_1 = vshrn_n_s32(_acc_1, 2);

            uint8x8_t _D = vqmovun_s16(vcombine_s16(_acc16, _acc16_1));

            vst1_u8(Dp, _D);

            Dp += 8;
            rows0p += 8;
            rows1p += 8;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "vdup.s16   d16, %8         \n"
                "mov        r4, #2          \n"
                "vdup.s16   d17, %9         \n"
                "vdup.s32   q12, r4         \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "0:                         \n"
                "vmull.s16  q0, d2, d16     \n"
                "vmull.s16  q1, d3, d16     \n"
                "vorr.s32   q10, q12, q12   \n"
                "vorr.s32   q11, q12, q12   \n"
                "vmull.s16  q2, d6, d17     \n"
                "vmull.s16  q3, d7, d17     \n"
                "vsra.s32   q10, q0, #16    \n"
                "vsra.s32   q11, q1, #16    \n"
                "pld        [%0, #128]      \n"
                "vld1.s16   {d2-d3}, [%0 :128]!\n"
                "vsra.s32   q10, q2, #16    \n"
                "vsra.s32   q11, q3, #16    \n"
                "pld        [%1, #128]      \n"
                "vld1.s16   {d6-d7}, [%1 :128]!\n"
                "vshrn.s32  d20, q10, #2    \n"
                "vshrn.s32  d21, q11, #2    \n"
                "vqmovun.s16 d20, q10        \n"
                "vst1.8     {d20}, [%2]!    \n"
                "subs       %3, #1          \n"
                "bne        0b              \n"
                "sub        %0, #16         \n"
                "sub        %1, #16         \n"
                : "=r"(rows0p), // %0
                "=r"(rows1p), // %1
                "=r"(Dp),     // %2
                "=r"(nn)      // %3
                : "0"(rows0p),
                "1"(rows1p),
                "2"(Dp),
                "3"(nn),
                "r"(b0), // %8
                "r"(b1)  // %9
                : "cc", "memory", "r4", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11", "q12");
        }
#endif // __aarch64__
#endif // __ARM_NEON
        for (; remain; --remain) {
            //             D[x] = (rows0[x]*b0 + rows1[x]*b1) >> INTER_RESIZE_COEF_BITS;
            *Dp++ = (unsigned char) (((short) ((b0 * (short) (*rows0p++)) >> 16) +
                                      (short) ((b1 * (short) (*rows1p++)) >> 16) + 2) >> 2);
        }

        ibeta += 2;
    }

    delete[] buf;
}


struct Point {
    float fX;
    float fY;

    void set(float x, float y) {
        fX = x;
        fY = y;
    }
};

static inline float __clamp(float v, float minV, float maxV) {
    return std::max(std::min(v, maxV), minV);
}

void
ImageResizeHelper::resizeC1Nearest(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                                   int oh) {
    const uint8_t *src = input;
    for (int i = 0; i < oh; ++i) {
        uint8_t *dstTemp = dst + i * ow;

        Point points[2];
        points[0].fX = 0;
        points[0].fY = (float) i * (float) ih / (float) oh;

        points[1].fX = (float) iw / (float) ow;
        points[1].fY = 0;

        MNNSamplerC1NearestOpt(src, dstTemp, (float *) (points), ow, iw, ih, iw);
    }
}


void ImageResizeHelper::resizeC1Bilinear(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                                         int oh) {
    auto *src = input;
    for (int i = 0; i < oh; ++i) {
        uint8_t *dstTemp = dst + i * ow;

        Point points[2];
        points[0].fX = 0;
        points[0].fY = (float) i * (float) ih / (float) oh;

        points[1].fX = (float) iw / (float) ow;
        points[1].fY = 0;

        MNNSamplerC1BilinearOpt(src, dstTemp, (float *) (points), ow, iw - 1, ih - 1, iw);
    }
}

void
ImageResizeHelper::resizeYuvBilinear(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                                     int oh) {
    resizeC1Bilinear(input, iw, ih, dst, ow, oh);
    uint8_t *dstUV = dst + ow * oh;
    auto src = input + iw * ih;
    for (int i = 0; i < oh / 2; ++i) {
        uint8_t *dstTemp = dstUV + i * ow;

        Point points[2];
        points[0].fX = 0;
        points[0].fY = (float) i * (float) ih / (float) oh;

        points[1].fX = (float) iw / (float) ow;
        points[1].fY = 0;

        Point curPoints;
        curPoints.fX = points[0].fX;
        curPoints.fY = points[0].fY;

        float dy = points[1].fY;
        float dx = points[1].fX;

        float xMax = (float) iw / 2 - 1;
        float yMax = (float) ih / 2 - 1;

        for (int j = 0; j < ow / 2; ++j) {
            float y = __clamp(curPoints.fY, 0, yMax);
            float x = __clamp(curPoints.fX, 0, xMax);
            int y0 = (int) y;
            int x0 = (int) x;
            int y1 = (int) ceilf(y);
            int x1 = (int) ceilf(x);
            float xF = x - (float) x0;
            float yF = y - (float) y0;
            for (int b = 0; b < 2; ++b) {
                unsigned char c00 = src[y0 * iw + 2 * x0 + b];
                unsigned char c01 = src[y0 * iw + 2 * x1 + b];
                unsigned char c10 = src[y1 * iw + 2 * x0 + b];
                unsigned char c11 = src[y1 * iw + 2 * x1 + b];

                float v =
                        (1.0f - xF) * (1.0f - yF) * c00 + xF * (1.0f - yF) * c01 +
                        yF * (1.0 - xF) * c10 + xF * yF * (c11);
                v = std::min(std::max(v, 0.0f), 255.0f);
                dstTemp[2 * j + b] = (unsigned char) v;
            }
            curPoints.fY += dy;
            curPoints.fX += dx;
        }
    }
}

void
ImageResizeHelper::resizeNearestYuv(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                                    int oh) {
    resizeC1Nearest(input, iw, ih, dst, ow, oh);

    uint8_t *dstUV = dst + ow * oh;
    auto src = input + iw * ih;
    for (int i = 0; i < oh / 2; ++i) {
        uint8_t *dstTemp = dstUV + i * ow;

        Point points[2];
        points[0].fX = 0;
        points[0].fY = (float) i * (float) ih / (float) oh;

        points[1].fX = (float) iw / (float) ow;
        points[1].fY = 0;

        Point curPoints;
        curPoints.fX = points[0].fX;
        curPoints.fY = points[0].fY;

        float dy = points[1].fY;
        float dx = points[1].fX;

        float xMax = (float) iw / 2 - 1;
        float yMax = (float) ih / 2 - 1;

        for (int j = 0; j < ow / 2; ++j) {
            int y = (int) roundf(__clamp(curPoints.fY, 0, yMax));
            int x = (int) roundf(__clamp(curPoints.fX, 0, xMax));
            curPoints.fY += dy;
            curPoints.fX += dx;
            auto sourcePos = y * iw + 2 * x;
            for (int k = 0; k < 2; ++k) {
                dstTemp[2 * j + k] = src[sourcePos + k];
            }
        }
    }
}


void
ImageResizeHelper::cropC1(const uint8_t *input, uint8_t *output, int inputWidth, int left, int top,
                          int width,
                          int height) {
    const uint8_t *src = input + top * inputWidth + left;
    uint8_t *dst = output;
    for (int i = 0; i < height; ++i) {
        memcpy(dst, src, width);
        src += inputWidth;
        dst += width;
    }
}

void
ImageResizeHelper::cropC3(const uint8_t *input, uint8_t *output, int inputWidth, int left, int top,
                          int width,
                          int height) {
    const uint8_t *src = input + top * inputWidth * 3 + left * 3;
    uint8_t *dst = output;
    for (int i = 0; i < height; ++i) {
        memcpy(dst, src, width * 3);
        src += inputWidth * 3;
        dst += width * 3;
    }
}

void
ImageResizeHelper::cropYuv(const uint8_t *input, uint8_t *output, int inputWidth, int inputHeight,
                           int left, int top,
                           int width,
                           int height) {
    const uint8_t *src = input + top * inputWidth + left;
    uint8_t *dst = output;
    for (int i = 0; i < height; ++i) {
        memcpy(dst, src, width);
        src += inputWidth;
        dst += width;
    }

    int a = inputWidth * inputHeight + (top / 2) * inputWidth + left;
    int b = (a >> 1) * 2;
    src = input + b;
    uint8_t *dstuv = output + width * height;
    for (int i = 0; i < height / 2; ++i) {
        memcpy(dstuv, src, width);
        src += inputWidth;
        dstuv += width;
    }
}
