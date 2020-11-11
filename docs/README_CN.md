
![TengineKit](https://openailab.oss-cn-shenzhen.aliyuncs.com/logo/TengineKit.png?raw=true "TengineKit logo")
=======================================================================    

# TengineKit - 永久免费移动端实时人脸212关键点SDK
[![Apache 2.0](https://img.shields.io/crates/l/r)](LICENSE)

TengineKit, 由 OPEN AI LAB 自主研发.        
TengineKit是一个易于集成的人脸检测和人脸关键点SDK。目前，它可以在各种手机上以非常低的延迟运行。**我们会持续更新这个项目，让效果更佳，性能更好！**

# 效果

| 人脸检测 &</br> 人脸2d关键点 | 人脸3d关键点 &</br>虹膜 | 上半身检测 &</br> 上半身关键点 | 手检测 &</br> 手关键点 |
| :---: | :---: | :---: | :---: |
| <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/TengineKitDemo4.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/face2.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/body3.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/hand2.gif"/></div> |

## Gif
<div align=center><img width="568" height="320"   src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/object_face_landmark.gif"/></div>
<div align=center><b>跳舞主持</b></div>

## 视频( <a href="https://www.youtube.com/watch?v=bnyD3laX_bU" target="_blank">YouTube</a> | <a href="https://www.bilibili.com/video/BV1AK4y147xx/" target="_blank">BiliBili</a> )
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
- 人脸3d关键点
- 人脸属性像性别，年龄，是否戴眼镜，是否微笑，颜值
- 眼睛眼球及眼眶的检测和关键点
- 手部检测(尚未手机实时)
- 手部关键点(尚未手机实时)
- 身体检测(尚未手机实时)
- 身体关键点(尚未手机实时)

# 性能指标(face & landmark)

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

# 配置Gradle
Project中的build.gradle添加
```implementation
    repositories {
        ...
        jcenter()
        mavenCentral()
        ...
    }

    allprojects {
        repositories {
            ...
            jcenter()
            mavenCentral()
            ...    
        }
    }
    
```     
主Module中的```build.gradle```添加,选择你需要的功能。```core```是必须的。
```java
    dependencies {
        ...
        // Must!!!!
        implementation 'com.tengine.tenginekit:core:0.0.1'
        // Function on Face
        implementation 'com.tengine.tenginekit:face:0.0.1'
        // Function on Hand
        implementation 'com.tengine.tenginekit:hand:0.0.1'
        // Function on Body
        implementation 'com.tengine.tenginekit:body:0.0.1'
        ...
    } 

# 系统
Android
- 系统最低要求API 19

# API
在使用sdk时，您可以参考[Api](Api_CN.md)来完成你需要的功能。

# 人脸关键点顺序
[人脸关键点顺序](POINTORDER_CN.md)

# 使用
您可以伴随着您给我们的```star```一起看看[使用示例](Usage.md)，感谢。

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
“sample/”文件夹下的Android代码。
# 接入指南
在TengineKit Api的setRotation 里有两个有两个参数ori和is_screen_rotate，分别为旋转角度和是否跟随屏幕旋转。这个是否跟随屏幕在Manifest里面的android:screenOrientation参数可以设置。不设置这个参数就是跟随屏幕旋转。
# 处理过程
## 1.设备预览
这部分是从Camera获取数据，作为SDK的输入。
## 2.角度
我们以竖直屏幕下为0度角，由于Android摄像头采集的数据实际总是偏差90，所以在设置ori参数的时候都需要 + (-90)，安卓的实际旋转角是需要通过传感器加上函数计算去获得。具体可以看Demo项目中的示例。
## 3.渲染
在渲染的时候是以0°角去渲染，是人在正常情况下看到正常输出。Android部分有Canvas和Opengl渲染，选用Opengl渲染可以让你的apk效果更好。

# 联系
关于TengineKit的使用以及人脸相关的技术交流可以加入下群(加群答案：TengineKit)：
- TengineKit QQ交流群 630836519
- 扫码加群
 
 <img width="256" height="256"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/QQGroup_QR.jpg"/>

# 图像处理
[图像处理](Api_CN.md#图像处理文档)进行缩放、旋转、裁剪、改变图片格式。