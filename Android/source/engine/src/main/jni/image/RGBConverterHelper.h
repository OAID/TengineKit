//
// Created by Hebing Shi on 2021/7/27.
//

#ifndef TENGINEDEMO_RGBCONVERTERHELPER_H
#define TENGINEDEMO_RGBCONVERTERHELPER_H


class RGBConverterHelper {
public:
    static int rgba2RGB(const unsigned char *rgba, int w, int h, unsigned char *rgb);

    static void rgb2Gray(const unsigned char *source, unsigned char *dest, size_t count);
};


#endif //TENGINEDEMO_RGBCONVERTERHELPER_H
