# ifndef UTILS_HPP
# define UTILS_HPP

#ifndef M_PI
#define M_PI (3.1415926535f)
#endif
#define clip(x, y) (x < 0 ? 0 : (x > y ? y : x))


#include "math.h"
#include "cmath"
#include <sys/time.h>
#include <iostream>

double get_cur_time();

float normalize_radians(float angle);

float sigmoid(double src);

#endif