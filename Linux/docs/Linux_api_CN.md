## SDK初始化
```c++
void facesdk_init(FaceSDKConfig config);
```
配置个FaceSDKConfig传入。

```c++
typedef struct FaceSDKConfig
{
    int img_w;                  //图像宽
    int img_h;                  //图像高
    int screen_w;               //显示宽
    int screen_h;               //显示高

    ImageFormat input_format;   //输入图像格式
    FaceSDKMode mode;           //设置模型
    int thread_num;             //配置线程

} FaceSDKConfig;
```

## 读取模型
```c++
/*!
* @brief Read Model , Every Function You Need Set ModelType First
*
* @param [in] type: Function.
* @param [in] model_path: Model Path.
* @param [in] modelInputFormat: Frame image format.
*/
void facesdk_readModelFromFile(ModelType type, const char* model_path, ImageFormat modelInputFormat);
```

## 人脸检测
```c++
/*!
* @brief Face Detect
*
* @param [in] imgData: Image Data.
*/
sdkFaces facesdk_detect(char *imgData);
```

## 人脸关键点
```c++
/*!
* @brief Face Landmarks, 212 points
*/
sdkFaces facesdk_landmark();
```

## 人脸3d关键点
```c++
/*!
* @brief Read Face 3d Landmarks,468 points
*/
sdkFaces3d facesdk_landmark3d();
```

## 人脸属性
```c++
/*!
* @brief Face attribution
*/
sdkFaces facesdk_attribute();
```

## 虹膜关键点
```c++
/*!
* @brief Iris landmarks,76 points
*/
sdkFaces3d facesdk_eyelandmark3d();
```

## 身体检测
```c++
/*!
* @brief Body detect
*
* @param [in] imageData: Image data.
*/
sdkBody facesdk_bodydetect(char *imageData);
```

## 身体关键点
```c++
/*!
* @brief Body landmarks, 25 points
*/
sdkBody facesdk_bodylandmark();
```

## 手部检测
```c++
/*!
* @brief Hand detect
*
* @param [in] imageData: Image data.
*/
sdkHand3d facesdk_handdetect(char *imageData);
```

## 手部关键点
```c++
/*!
* @brief Hand landmarks, 21 points
*/
sdkHand3d facesdk_handlandmark3d();
```

## Yolov5
```c++
/*!
* @brief yolov5 Use
*/
sdkYolov5 facesdk_yolov5();
```

## 释放
```c++
/*!
* @brief Release SDK
*
*/
void facesdk_release();
```