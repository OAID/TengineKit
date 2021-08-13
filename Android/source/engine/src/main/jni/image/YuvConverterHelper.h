//
// Created by Hebing Shi on 2021/7/27.
//

#ifndef TENGINEDEMO_YUVCONVERTERHELPER_H
#define TENGINEDEMO_YUVCONVERTERHELPER_H


class YuvConverterHelper {
public:
    static void nv21RGB(const unsigned char *src, unsigned char *dst, int w, int h);
};


#endif //TENGINEDEMO_YUVCONVERTERHELPER_H
