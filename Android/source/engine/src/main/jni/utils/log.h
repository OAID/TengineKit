//
// Created by Hebing Shi on 2021/7/26.
//

#ifndef TENGINEDEMO_LOG_H
#define TENGINEDEMO_LOG_H

#include <jni.h>
#include <android/log.h>


#define  LOG_TAG    "TengineKitJni"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)

#endif //TENGINEDEMO_LOG_H
