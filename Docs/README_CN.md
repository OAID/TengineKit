
![TengineKit](https://openailab.oss-cn-shenzhen.aliyuncs.com/logo/TengineKit.png?raw=true "TengineKit logo")
=======================================================================    

[![Apache 2.0](https://img.shields.io/crates/l/r)](LICENSE)

TengineKit, 由 OPEN AI LAB 自主研发.        
TengineKit是一个易于集成的AI算法SDK。目前，它可以在各种手机上以非常低的延迟运行。**我们会持续更新这个项目，让效果更佳，性能更好！**

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
- [Apk](../Android/apk/TengineKitDemo-v1.0.3.apk) 可直接下载安装在手机上看到效果。

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

# 性能指标(Face Detect & Face Landmark)(缩短了时间)

| CPU | 耗时 | 帧率 |
| :---: | :---: | :---: |
| 麒麟980 | 4ms | 250fps | 
| 骁龙855 | 5ms | 200fps |
| 麒麟970 | 7ms | 142fps |
| 骁龙835 | 8ms | 125fps |
| 麒麟710F| 9ms | 111fps |
| 骁龙439 | 16ms | 62fps |
| 联发科 Helio P60 | 17ms | 59fps |
| 骁龙450B | 18ms | 56fps |


# 人脸关键点顺序
[人脸关键点顺序](POINTORDER_CN.md)

# 联系
关于TengineKit的使用以及人脸相关的技术交流可以加入下群(加群答案：TengineKit)：
- TengineKit QQ交流群 630836519
- 扫码加群
 
 <img width="256" height="256"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/QQGroup_QR.jpg"/>