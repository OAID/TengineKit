
![TengineKit](https://openailab.oss-cn-shenzhen.aliyuncs.com/logo/TengineKit.png?raw=true "TengineKit logo")
=======================================================================

# TengineKit
[![Apache 2.0](https://img.shields.io/crates/l/r)](LICENSE)
<br>
<br>
TengineKit, developed by OPEN AI LAB。
TengineKit是一个易于集成的人脸检测和人脸关键点SDK。目前，它可以在各种手机上以非常低的延迟运行。

# 效果

## DEMO

<div align=center><img width="400" height="857"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/TengineKitDemo2.gif"/></div>
<div align=center><b>真实效果</b></div>

## Gif
<div align=center><img width="800" height="400"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/object_face_landmark.gif"/></div>
<div align=center><b>跳舞主持</b></div>

## 视频
[<div align=center><img width="800" height="400" src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/landmark_report.png"/></div>](https://www.bilibili.com/video/BV1AK4y147xx/)

# 试用
- [Apk](apk/TengineKitDemo-v1.2.2.apk) 可直接下载安装在手机上看到效果。

或者

- android扫码直接下载

![Apk](https://www.pgyer.com/app/qrcode/A0uD?sign=&auSign=&code=)

# 目标
- 移动端最佳的性能的人脸SDK
- 移动端最简单易用的API
- 移动端最小的包体

# 功能
- 人脸检测
- 人脸关键点
- 人脸属性像性别，年龄，是否戴眼镜，是否微笑，颜值

# 性能指标

| CPU | 耗时 | 帧率 |
| :---: | :---: | :---: |
| 麒麟980 | 14ms | 71fps | 
| 骁龙855 | 15ms | 67fps |
| 麒麟970 | 17ms | 59fps |
| 骁龙835 | 18ms | 56fps |
| 麒麟710F| 19ms | 53fps |
| 骁龙439 | 26ms | 38fps |
| 联发科 Helio P60 | 27ms | 37fps |
| 骁龙450B | 28ms | 36fps |

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

# 系统
Android
- 系统最低要求API 19

# API
方法位于```com.tenginekit.FaceManager```下。
## 初始化

#### 参数
 - context：activity的context 
 - preview_w：摄像头预览宽
 - preview_h：摄像头预览高
 - screen_w：屏幕宽
 - screen_h：屏幕高
 - format：数据格式(YUV, RGB)

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


## 得到人脸信息

#### 参数
 - imageData：输入数据

``` java
    FaceInfo[] info = FaceManager.getInstance().getFaceInfo(byte[] imageData);
```

## 设置旋转角

#### 参数
 - ori：旋转角度(以竖直为0°)
 - is_screen_rotate：是否展示区域跟随旋转
 - screen_w：屏幕宽
 - screen_h：屏幕高

``` java
    FaceManager.getInstance().setRotation(int ori, boolean is_screen_rotate, float screen_w, float screen_h);
```


## 切换摄像头

#### 参数
 - back：是否是后置摄像头。

``` java
    FaceManager.getInstance().switchCamera(boolean back);
```


## 开启额外功能

#### 参数：
 - FuncName：额外的功能类型
 - state：是否开启

``` java
    FaceManager.getInstance().changeFuncState(ExtraFunc FuncName, boolean state);
```
``` java
    enum ExtraFunc
    {
        Attribution, // 人脸属性功能
    }
```


## 释放
``` java
    FaceManager.getInstance().release();
```

# 数据结构
- FaceInfo

| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| faceRect | Rect | 人脸矩形框 |
| landmarks | List<LandmarkInfo> | 人脸关键点信息 212个点 |
| xAngle | float | 人脸x方向转角 |
| yAngle | float | 人脸y方向转角 |
| zAngle | float | 人脸z方向转角 |
| leftEyeClose | float | 左眼闭合置信度  0~1 |
| rightEyeClose | float | 右眼闭合置信度  0~1 |
| mouseClose | float | 嘴巴闭合置信度  0~1 |
| mouseOpenBig | float | 嘴巴张大置信度  0~1 |

- FaceInfo --属性功能

| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| age | int | 年龄 |
| isMan | boolean | 是否是男性 |
| smile | int | 微笑程度0~100 |
| glasses | boolean | 是否佩戴眼镜 |
| beatyOfManLook | int | 男性角度看的颜值 |
| beautyOfWomanLook | int | 女性角度看的颜值 |

- LandmarkInfo

| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| x | float | 点的x位置信息 |
| y | float | 点的y位置信息 |

# 人脸关键点顺序
[人脸关键点顺序](POINTORDER_CN.md)

# 权限
``` permission
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<uses-permission android:name="android.permission.CAMERA"/>
```

# 示例
“sample/Android”文件夹下的Android代码。
# 接入指南
在TengineKit Api的setRotation 里有两个有两个参数ori和is_screen_rotate，分别为旋转角度和是否跟随屏幕旋转。这个是否跟随屏幕在Manifest里面的android:screenOrientation参数可以设置。不设置这个参数就是跟随屏幕旋转。
# 处理过程
## 1.设备预览
这部分是从Camera获取数据，作为SDK的输入。
## 2.角度
我们以竖直屏幕下为0度角，由于Android摄像头采集的数据实际总是偏差90，所以在设置ori参数的时候都需要 + (-90)，安卓的实际旋转角是需要通过传感器加上函数计算去获得。具体可以看Demo项目中的示例。
![process](https://openailab.oss-cn-shenzhen.aliyuncs.com/images/process_graph.png)
## 3.渲染
在渲染的时候是以0°角去渲染，是人在正常情况下看到正常输出。Android部分有Canvas和Opengl渲染，选用Opengl渲染可以让你的apk效果更好。

# 联系
关于TengineKit的使用以及人脸相关的技术交流可以加入下群(加群答案：TengineKit)：
- TengineKit QQ交流群 630836519
- 扫码加群
 
 <img width="256" height="256"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/QQGroup_QR.jpg"/>
