//
// Created by Hebing Shi on 2021/7/26.
//

#ifndef TENGINEDEMO_IMAGERESIZEHELPER_H
#define TENGINEDEMO_IMAGERESIZEHELPER_H


#include <jni.h>

extern "C" {
void MNNSamplerC1NearestOpt(const unsigned char *source, unsigned char *dest, float *points,
                            size_t count, size_t iw,
                            size_t ih, size_t yStride);

void MNNSamplerC1BilinearOpt(const unsigned char *source, unsigned char *dest, float *points,
                             size_t count, size_t xMax,
                             size_t yMax, size_t yStride);
}

class ImageResizeHelper {
public:

    static void
    resize_bilinear_c4(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w,
                       int h);

    static void
    resize_bilinear_c4(const unsigned char *src, int srcw, int srch, int srcstride,
                       unsigned char *dst,
                       int w, int h, int stride);

    static void
    resize_bilinear_c1(const unsigned char *src, int srcw, int srch, int srcstride,
                       unsigned char *dst,
                       int w, int h, int stride);

    static void
    resize_bilinear_c3(const unsigned char *src, int srcw, int srch, int srcstride,
                       unsigned char *dst,
                       int w, int h, int stride);

    void
    resize_bilinear_c1(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w,
                       int h);

    void
    resize_bilinear_c3(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w,
                       int h);

    static void
    resize_bilinear_c2(const unsigned char *src, int srcw, int srch, int srcstride,
                       unsigned char *dst,
                       int w, int h, int stride);

    static void
    resizeNearestYuv(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow, int oh);

    static void
    resizeYuvBilinear(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow, int oh);

    static void
    resizeC1Nearest(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                    int oh);

    static void resizeC1Bilinear(const uint8_t *input, int iw, int ih, uint8_t *dst, int ow,
                                 int oh);

    static void
    cropC1(const uint8_t *input, uint8_t *output, int inputWidth, int left, int top, int width,
           int height);

    static void
    cropC3(const uint8_t *input, uint8_t *output, int inputWidth, int left, int top, int width,
           int height);



    static void
    cropYuv(const uint8_t *input, uint8_t *output, int inputWidth, int inputHeight, int left,
            int top,
            int width, int height);
};


#endif //TENGINEDEMO_IMAGERESIZEHELPER_H
