#include <jni.h>
#include <log.h>
#include "core/manager.hpp"
#include "ImageResizeHelper.h"
#include "YuvConverterHelper.h"
#include "ImageRotateHelper.h"
#include "RGBConverterHelper.h"
#include "fstream"

#ifdef DEBUG_IMAGE

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>

#endif

#include <android/bitmap.h>

#ifdef __cplusplus
extern "C" {
#endif

// use for save handler addr
// jni handler
jfieldID jniFieldHandler;
// Face
jfieldID jX1Field;
jfieldID jY1Field;
jfieldID jX2Field;
jfieldID jY2Field;
jfieldID jLandMarkField;
jfieldID jHeadXField;
jfieldID jHeadYField;
jfieldID jHeadZField;
jfieldID jLeftEyeCloseField;
jfieldID jRightEyeCloseField;
jfieldID jMouthCloseField;
jfieldID jMouthBigOpenField;
jfieldID jLandMark3dField;
jmethodID jFaceConstructMethodId;
jclass jFaceClass;
// ImageConfig///////////////////
//int degree;
//FaceImageFormat format;
//Bitmap bitmap;
//int width;
//int height;
//byte[] data;
jfieldID jDegreeField;
jfieldID jFaceImageFormatField;
jfieldID jFormatIntFiled;
jfieldID jWidthField;
jfieldID jHeightField;
jfieldID jDataField;
jfieldID jMirrorField;

//public boolean detect;
//public boolean landmark2d;
//public int maxFaceNum = 100;
jfieldID jDetectField;
jfieldID jBoolLandMarkField;
jfieldID jMaxFaceNumField;
jfieldID jVideo;


static void setManagerObject(JNIEnv *env, jobject obj, manager *manager) {
    env->SetLongField(obj, jniFieldHandler, (jlong) manager);
}

manager *getManagerObject(JNIEnv *env, jobject obj) {
    return (manager *) env->GetLongField(obj, jniFieldHandler);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("%s", __func__);

    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("env error");
        return -1;
    }

    //提前获取一些字段避免多余的计算
    jclass clz = env->FindClass("com/tenginekit/engine/core/TengineKitEngine");
    jniFieldHandler = env->GetFieldID(clz, "mJniHandler", "J");
    env->DeleteLocalRef(clz);

    clz = env->FindClass("com/tenginekit/engine/face/Face");
    jX1Field = env->GetFieldID(clz, "x1", "F");
    jY1Field = env->GetFieldID(clz, "y1", "F");
    jX2Field = env->GetFieldID(clz, "x2", "F");
    jY2Field = env->GetFieldID(clz, "y2", "F");
    jLandMarkField = env->GetFieldID(clz, "landmark", "[F");
    jHeadXField = env->GetFieldID(clz, "headX", "F");
    jHeadYField = env->GetFieldID(clz, "headY", "F");
    jHeadZField = env->GetFieldID(clz, "headZ", "F");
    jLeftEyeCloseField = env->GetFieldID(clz, "leftEyeClose", "F");
    jRightEyeCloseField = env->GetFieldID(clz, "rightEyeClose", "F");
    jMouthCloseField = env->GetFieldID(clz, "mouthClose", "F");
    jMouthBigOpenField = env->GetFieldID(clz, "mouthBigOpen", "F");
    jLandMark3dField = env->GetFieldID(clz, "landmark3d", "[F");
    jFaceConstructMethodId = env->GetMethodID(clz, "<init>", "()V");
    jFaceClass = (jclass) (env->NewGlobalRef(clz));

    clz = env->FindClass("com/tenginekit/engine/core/ImageConfig");
    jDegreeField = env->GetFieldID(clz, "degree", "I");
    jFaceImageFormatField = env->GetFieldID(clz, "format",
                                            "Lcom/tenginekit/engine/core/ImageConfig$FaceImageFormat;");
    jclass imageFormat = env->FindClass("com/tenginekit/engine/core/ImageConfig$FaceImageFormat");
    jFormatIntFiled = env->GetFieldID(imageFormat, "value", "I");
    jWidthField = env->GetFieldID(clz, "width", "I");
    jHeightField = env->GetFieldID(clz, "height", "I");
    jDataField = env->GetFieldID(clz, "data", "[B");
    jMirrorField = env->GetFieldID(clz, "mirror", "Z");

    clz = env->FindClass("com/tenginekit/engine/face/FaceConfig");
    jDetectField = env->GetFieldID(clz, "detect", "Z");
    jBoolLandMarkField = env->GetFieldID(clz, "landmark2d", "Z");
    jMaxFaceNumField = env->GetFieldID(clz, "maxFaceNum", "I");
    jVideo = env->GetFieldID(clz, "video", "Z");

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL
Java_com_tenginekit_engine_core_TengineKitEngine_init(JNIEnv *env, jobject object,
                                                      jstring model_path, jobject sdk_config) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    env->ReleaseStringUTFChars(model_path, path);
    manager *engine = getManagerObject(env, object);
    if (engine == nullptr) {
        engine = new manager();
        setManagerObject(env, object, engine);
    }
    engine->Init(std::string(path));
}

bool hasSave = false;


void
preProcessImageData(uint8_t *rgbForDetect, uint8_t *yuvForDetect, uint8_t *input, int degree,
                    int inputWidth,
                    int inputHeight, int format, FaceConfig &faceConfig, int &detectWidth,
                    int &detectHeight) {
    switch (format) {
        case ImageFormat::YUV:
            LOGI("start rotate");
            if (degree == 270) {
                ImageRotateHelper::rotateNv270(
                        input,
                        inputWidth,
                        inputHeight,
                        yuvForDetect,
                        inputHeight,
                        inputWidth
                );
                detectHeight = inputWidth;
                detectWidth = inputHeight;
            } else if (degree == 90) {
                ImageRotateHelper::rotateNv90(
                        input,
                        inputWidth,
                        inputHeight,
                        yuvForDetect,
                        inputHeight,
                        inputWidth
                );
                detectHeight = inputWidth;
                detectWidth = inputHeight;
            } else if (degree == 180) {
                ImageRotateHelper::rotateNv180(input, inputWidth, inputHeight, yuvForDetect,
                                               inputWidth, inputHeight);
                detectHeight = inputHeight;
                detectWidth = inputWidth;
            } else {
                memcpy(yuvForDetect, input, inputWidth * inputHeight * 3 / 2);
                detectHeight = inputHeight;
                detectWidth = inputWidth;
            }
            LOGI("end rotate");

            LOGI("start yuv rgb");
            YuvConverterHelper::nv21RGB(yuvForDetect, rgbForDetect, detectWidth, detectHeight);
            LOGI("end yuv rgb");
#ifdef DEBUG_IMAGE
            if (!hasSave) {
                std::ofstream out(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/origin.bin",
                        std::ofstream::binary);
                out.write(reinterpret_cast<const char *>(input), inputWidth * inputHeight * 3 / 2);
                out.close();

                cv::Mat mat0 = cv::Mat(inputHeight * 3 / 2, inputWidth, CV_8UC1, input);
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/run0.png",
                        mat0);

                cv::Mat mat1 = cv::Mat(inputWidth * 3 / 2, inputHeight, CV_8UC1, yuvForDetect);
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/run1.png",
                        mat1);
                hasSave = true;

                cv::Mat mat2 = cv::Mat(detectHeight, detectWidth, CV_8UC3, rgbForDetect);
                cv::cvtColor(mat2, mat2, CV_RGB2BGR);
                cv::imwrite(
                        "/storage/emulated/0/Android/data/com.tenginekit.tenginedemo/cache/runRGB.png",
                        mat2);
                hasSave = true;
            }
#endif
            break;
        case ImageFormat::RGB:
            memcpy(rgbForDetect, input, inputWidth * inputHeight * 3);
            RGBConverterHelper::rgb2Gray(rgbForDetect, yuvForDetect, inputWidth * inputHeight);
            detectHeight = inputHeight;
            detectWidth = inputWidth;
            break;
        case ImageFormat::RGBA:
            RGBConverterHelper::rgba2RGB(input, inputWidth, inputHeight, rgbForDetect);
            RGBConverterHelper::rgb2Gray(rgbForDetect, yuvForDetect, inputWidth * inputHeight);
            detectHeight = inputHeight;
            detectWidth = inputWidth;
            break;
        default:
            LOGE("do not support this format current now");
            break;
    }
}


JNIEXPORT jobjectArray JNICALL
Java_com_tenginekit_engine_core_TengineKitEngine_nativeDetectFace(JNIEnv *env, jobject thiz,
                                                                  jobject jImageConfig,
                                                                  jobject jfaceConfig) {
    FaceConfig faceConfig;
    faceConfig.detect = env->GetBooleanField(jfaceConfig, jDetectField);
    faceConfig.landmark = env->GetBooleanField(jfaceConfig, jBoolLandMarkField);
    bool isVideo = env->GetBooleanField(jfaceConfig, jVideo);
    faceConfig.useYuvForResize = isVideo;
    auto inputByteArray = (jbyteArray) (env->GetObjectField(jImageConfig,
                                                            jDataField));
    int inputWidth = env->GetIntField(jImageConfig, jWidthField);
    int inputHeight = env->GetIntField(jImageConfig, jHeightField);
    jobject jFormat = env->GetObjectField(jImageConfig, jFaceImageFormatField);
    int format = env->GetIntField(jFormat, jFormatIntFiled);
    int degree = env->GetIntField(jImageConfig, jDegreeField);
    faceConfig.mirror = env->GetBooleanField(jImageConfig, jMirrorField);

    LOGI("inputHeight:%d  inputWidth:%d", inputHeight, inputWidth);
    auto *inputData = (uint8_t *) env->GetByteArrayElements(inputByteArray, nullptr);
    auto *rgbForDetect = (uint8_t *) malloc(inputWidth * inputHeight * 3);
    auto *yuvForDetect = (uint8_t *) malloc(inputWidth * inputHeight * 3 / 2);
    int detectWidth, detectHeight;
    preProcessImageData(rgbForDetect, yuvForDetect, inputData, degree, inputWidth, inputHeight,
                        format, faceConfig, detectWidth, detectHeight);
    manager *engine = getManagerObject(env, thiz);
    if (engine == nullptr) {
        LOGE("engine null please check");
        return nullptr;
    }
    std::vector<FaceInfo> faces;
    engine->detect(rgbForDetect, yuvForDetect, faces, detectWidth, detectHeight, faceConfig);

    jobjectArray ret = nullptr;
    if (!faces.empty()) {
        ret = env->NewObjectArray((jsize) faces.size(), jFaceClass,
                                  nullptr);
        for (int i = 0; i < faces.size(); i++) {
            jobject face = env->NewObject(jFaceClass, jFaceConstructMethodId);
            env->SetFloatField(face, jX1Field, faces[i].face_box.x1 / 160);
            env->SetFloatField(face, jY1Field, faces[i].face_box.y1 / 120);
            env->SetFloatField(face, jX2Field, faces[i].face_box.x2 / 160);
            env->SetFloatField(face, jY2Field, faces[i].face_box.y2 / 120);

            jfloatArray array = env->NewFloatArray(212 * 2);
            env->SetFloatArrayRegion(array, 0, 212 * 2, faces[i].landmarks);
            env->SetObjectField(face, jLandMarkField, array);
            env->SetFloatField(face, jHeadXField, faces[i].headX);
            env->SetFloatField(face, jHeadYField, faces[i].headY);
            env->SetFloatField(face, jHeadZField, faces[i].headZ);
            env->SetFloatField(face, jLeftEyeCloseField, faces[i].leftEyeCloseState);
            env->SetFloatField(face, jRightEyeCloseField, faces[i].rightEyeCloseState);
            env->SetFloatField(face, jMouthCloseField, faces[i].mouthCloseState);
            env->SetFloatField(face, jMouthBigOpenField, faces[i].mouthBigOpenState);

            if (faceConfig.landmark3d) {
                jfloatArray array3d = env->NewFloatArray(468 * 2);
                auto landmark3d = new float[468 * 2];
                for (int j = 0; j < 468; ++j) {
                    landmark3d[2 * j] = faces[i].landmarks3d[3 * j];
                    landmark3d[2 * j + 1] = faces[i].landmarks3d[3 * j + 1];
                }
                env->SetFloatArrayRegion(array3d, 0, 468 * 2, landmark3d);
                delete[] landmark3d;
                env->SetObjectField(face, jLandMark3dField, array3d);
            }
            env->SetObjectArrayElement(ret, i, face);
            env->DeleteLocalRef(face);
        }
    }

    //组装返回值完成
    free(yuvForDetect);
    free(rgbForDetect);
    env->ReleaseByteArrayElements(inputByteArray, (jbyte *) (inputData), 0);
    return ret;
}


JNIEXPORT void JNICALL
Java_com_tenginekit_engine_core_TengineKitEngine_release(JNIEnv *env, jobject clazz) {
    manager *engine = getManagerObject(env, clazz);
    if (engine != nullptr) {
        delete engine;
    }
    env->SetLongField(clazz, jniFieldHandler, static_cast<jlong>(NULL));
}

#ifdef __cplusplus
}
#endif
