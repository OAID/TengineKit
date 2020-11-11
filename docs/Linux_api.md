## SDK init
```c++
void facesdk_init(FaceSDKConfig config);
```
Configure a FaceSDKConfig incoming.

```c++
typedef struct FaceSDKConfig
{
    int img_w;                  //Image Width
    int img_h;                  //Image Height
    int screen_w;               //Screen Width
    int screen_h;               //Screen Height

    ImageFormat input_format;   //Input Image Format
    FaceSDKMode mode;           //SDK Mode
    int thread_num;             //Thread

} FaceSDKConfig;
```

## Read Model
```c++
/*!
* @brief Read Model
*
* @param [in] type: Function.
* @param [in] model_path: Model Path.
* @param [in] modelInputFormat: Frame image format.
*/
void facesdk_readModelFromFile(ModelType type, const char* model_path, ImageFormat modelInputFormat);
```

## Face Detect
```c++
/*!
* @brief Face Detect
*
* @param [in] imgData: Image Data.
*/
sdkFaces facesdk_detect(char *imgData);
```

## Face Landmark
```c++
/*!
* @brief Face Landmarks, 212 points
*/
sdkFaces facesdk_landmark();
```

## Face 3d Landmark
```c++
/*!
* @brief Read Face 3d Landmarks,468 points
*/
sdkFaces3d facesdk_landmark3d();
```

## Face Attribution
```c++
/*!
* @brief Face attribution
*/
sdkFaces facesdk_attribute();
```

## Iris Landmark
```c++
/*!
* @brief Iris landmarks,76 points
*/
sdkFaces3d facesdk_eyelandmark3d();
```

## Body Detect
```c++
/*!
* @brief Body detect
*
* @param [in] imageData: Image data.
*/
sdkBody facesdk_bodydetect(char *imageData);
```

## Body Landmark
```c++
/*!
* @brief Body landmarks, 25 points
*/
sdkBody facesdk_bodylandmark();
```

## Hand Detect
```c++
/*!
* @brief Hand detect
*
* @param [in] imageData: Image data.
*/
sdkHand3d facesdk_handdetect(char *imageData);
```

## Hand Landmark
```c++
/*!
* @brief Hand landmarks, 21 points
*/
sdkHand3d facesdk_handlandmark3d();
```

## Release
```c++
/*!
* @brief Release SDK
*
*/
void facesdk_release();
```