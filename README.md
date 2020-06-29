[中文版本](README_CN.md)

![TengineKit](https://openailab.oss-cn-shenzhen.aliyuncs.com/logo/TengineKit.png?raw=true "TengineKit logo")
=======================================================================

# TengineKit
[![Apache 2.0](https://img.shields.io/crates/l/r)](LICENSE)
<br>
<br>
TengineKit, developed by OPEN AI LAB. Tenginekit is an easy-to-integrate face detection and face landmarks SDK. At present, it can run on various mobile phones at very low latency.

# Effect

## DEMO
<div align=center><img width="400" height="857"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/TengineKitDemo2.gif"/></div>
<div align=center><b>real scene</b></div>

## Gif
<div align=center><img width="800" height="400"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/object_face_landmark.gif"/></div>
<div align=center><b>dance of host</b></div>

## Video
[<div align=center><img width="568" height="320" src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/landmark_report.png"/></div>](https://youtu.be/bnyD3laX_bU)
<div align=center><img src="https://img.shields.io/youtube/views/bnyD3laX_bU?style=social"/></div>

# Have a try
- [Apk](apk/TengineKitDemo-v1.2.2.apk) can be directly downloaded and installed on the phone to see the effect.

or

- scan code to download apk 

![Apk](https://www.pgyer.com/app/qrcode/A0uD?sign=&auSign=&code=)

# Goals
- Provide best performance in mobile client
- Provide the simplest API in mobile client
- Provide the smallest package  in mobile client

# Features
- face detection
- face landmarks
- face attributes like age, gender, smile, glasses


# Performance

| CPU | Time consuming | Frame rate |
| :---: | :---: | :---: |
| Kirin 980 | 14ms | 71fps | 
| Qualcomm 855 | 15ms | 67fps |
| Kirin 970 | 17ms | 59fps |
| Qualcomm 835 | 18ms | 56fps |
| Kirin 710F| 19ms | 53fps |
| Qualcomm 439 | 26ms | 38fps |
| MediaTek Helio P60 | 27ms | 37fps |
| Qualcomm 450B | 28ms | 36fps |

# Usage
```java
...

import com.tenginekit.FaceManager;

public class CameraActivity extends AppCompatActivity implements Camera.PreviewCallback{
    private static final String TAG = "CameraActicity";

    // camera preview width
    protected int previewWidth;
    // camera preview height
    protected int previewHeight;
    // content display screen width
    public static float ScreenWidth;
    // content display screen height
    public static float ScreenHeight;

    // nv21 data from camera
    protected byte[] mNV21Bytes;

    ...

    public void Init() {
        mNV21Bytes = new byte[previewHeight * previewWidth];

        /**
         * init
         * */
        FaceManager.GetInstance().init(this,previewWidth, previewHeight, ScreenWidth, ScreenHeight,FaceManager.ImageFormat.YUV_NV21);
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }
        isProcessingFrame = true;
        try {
            if (mNV21Bytes == null) {

                ...

                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                Init();
                
                ...
            }
        } catch (final Exception e) {
            MyLogger.logError(TAG, "onPreviewFrame: " + e);
            return;
        }
        
        processImage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * release
         * */
        FaceManager.GetInstance().release();
    }


   

    protected void processImage() {

        ...

        FaceInfo[] faceInfos = FaceManager.GetInstance().getFaceInfo(mNV21Bytes);

        if (faceInfos != null && faceInfos.length > 0) {
            Rect[] face_rect = new Rect[faceInfos.length];
            List<List<LandmarkInfo>> face_landmarks = new ArrayList<>();
            for (int i = 0; i < faceInfos.length; i++) {
                face_rect[i] = faceInfos[i].faceRect;
                face_landmarks.add(faceInfos[i].landmarks);
            }

            // do something with face_rect, face_landmarks
        }

        ...

    }
}

```

# System
Android
- Min Sdk Version 19

# API
The located Function under ```com.tenginekit.FaceManager```.
## init

#### Parameter
 - context: context in activity，
 - preview_w: Camera preview width
 - preview_h: Camera preview height
 - screen_w: Screen width
 - screen_h: Screen height
 - format: Data format(YUV, RGB)

``` java
    FaceManager.getInstance().init(Context context, int preview_w, int preview_h, float screen_w, float screen_h, ImageFormat format);
```
``` java
    enum  ImageFormat
    {
        YUV_NV21,
        RGB,
        RGBA,
        GRAY
    }
```


## get face info

#### Parameter
 - imageData: Input data

``` java
    FaceInfo[] info = FaceManager.getInstance().getFaceInfo(byte[] imageData);
```


## set rotation

#### Parameter
 - ori: Rotation angle (vertical to 0°)
 - is_screen_rotate: whether the display area follows rotation
 - screen_w: screen width
 - screen_h: screen height

``` java
    FaceManager.getInstance().setRotation(int ori, boolean is_screen_rotate, float screen_w, float screen_h);
```


## switch camera

#### Parameter
 - back:Whether it is a rear camera

``` java
    FaceManager.getInstance().switchCamera(boolean back);
```

## turn on other function

#### Parameter
 - FuncName: Function name
 - state: Whether the additional function type is enabled


``` java
    FaceManager.getInstance().changeFuncState(ExtraFunc FuncName, boolean state);
```
``` java
    enum ExtraFunc
    {
        Attribution, // face attrubution Function
    }
```

## release
``` java
    FaceManager.getInstance().release();
```

# Data structure
- FaceInfo

| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| faceRect | Rect | Face rectangle |
| landmarks | List<LandmarkInfo> | Face key point information 212 points |
| xAngle | float | Human face x direction corner |
| yAngle | float | Human face y direction corner |
| zAngle | float | Human face z direction corner |
| leftEyeClose | float | Left eye closure confidence  0~1 |
| rightEyeClose | float | Right eye closure confidence  0~1 |
| mouseClose | float | Mouth closure confidence  0~1 |
| mouseOpenBig | float | Open mouth Big confidence  0~1 |

- FaceInfo -- Attribution 

| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| age | int | Age |
| isMan | boolean | Is it male |
| smile | int | Smile degree0~100 |
| glasses | boolean | Whether to wear glasses |
| beatyOfManLook | int | Face value from a male perspective |
| beautyOfWomanLook | int | Face value from a female perspective |

- LandmarkInfo

| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| x | float | Point x position |
| y | float | Point y position |

# Landmark Points Order
[LandmarksOrder](POINTORDER.md)

# Permission
``` permission
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<uses-permission android:name="android.permission.CAMERA"/>
```


# Sample
Android code under the "sample/Android" folder.
# Access Guide
In setRotation of TengineKit Api, there are two parameters ori and is_screen_rotate, which are the rotation angle and whether to follow the screen rotation. Whether the android:screenOrientation parameter in the Manifest follows the screen can be set. Not setting this parameter is to follow the screen rotation.
# Process
## 1.Device preview
This part is to get data from Camera, as the SDK input.
## 2.Angle
We use the vertical screen as an angle of 0 degrees. Since the data collected by the Android camera always deviates by 90, it is necessary to set + (-90) when setting the ori parameter. The actual rotation angle of Android is to add a function through the sensor Calculate to get. For details, see the example in the Demo project.
![process](https://openailab.oss-cn-shenzhen.aliyuncs.com/images/process_graph_EN.png)
## 3.Rendering
When rendering, it is rendered at an angle of 0°, which is the normal output that people see under normal circumstances. The Android part has Canvas and Opengl rendering. Using Opengl rendering can make your apk better.

# Contact
About the use of TengineKit and face-related technical exchanges, you can join the following QQ groups(Group Answer:TengineKit):
- TengineKit communication QQ group: 630836519
- Scan to join group
 
 <img width="256" height="256"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/QQGroup_QR.jpg"/>
