//
// Created by Hebing Shi on 2021/7/27.
//

#ifndef TENGINEDEMO_IMAGEROTATEHELPER_H
#define TENGINEDEMO_IMAGEROTATEHELPER_H


class ImageRotateHelper {
public:
    static void
    rotateNv270(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w,
                int h);

    static void
    mirrorC3(const unsigned char *src, int srcw, int srch, int srcstride,
             unsigned char *dst,
             int w, int, int stride);

    static void
    mirrorC1(const unsigned char *src, int srcw, int srch, int srcstride,
             unsigned char *dst,
             int w, int, int stride);

    static void
    rotateNv90(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w, int h);

    static void
    rotateNv180(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w, int h);

    static void mirrorNv(const unsigned char *src, int srcw, int srch, unsigned char *dst, int w, int h);
};


#endif //TENGINEDEMO_IMAGEROTATEHELPER_H
