[中文版本](Docs/README_CN.md)

![TengineKit](https://openailab.oss-cn-shenzhen.aliyuncs.com/logo/TengineKit.png?raw=true "TengineKit logo")
=======================================================================

[![Apache 2.0](https://img.shields.io/crates/l/r)](LICENSE)     

TengineKit, developed by OPEN AI LAB.       
TengineKit is an easy-to-integrate AI algorithm SDK. At present, it can run on various mobile phones at very low latency.**We will continue to update this project for better results and better performance!**

# Effect

| Face Detection &</br> Face 2dLandmark | Face 3dLandmark &</br>Iris | Upper Body Detection &</br> Uppper Body Landmark | Hand Detection &</br> Hand Landmark |
| :---: | :---: | :---: | :---: |
| <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/TengineKitDemo4.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/face2.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/body3.gif"/></div> | <div align=center><img width="150" height="270"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/hand2.gif"/></div> |



## Gif
<div align=center><img width="800" height="400"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/object_face_landmark.gif"/></div>
<div align=center><b>dance of host</b></div>

## Video( <a href="https://www.youtube.com/watch?v=bnyD3laX_bU" target="_blank">YouTube</a> | <a href="https://www.bilibili.com/video/BV1AK4y147xx/" target="_blank">BiliBili</a> )
[<div align=center><img width="568" height="320" src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/landmark_report.png"/></div>](https://youtu.be/bnyD3laX_bU)
<div align=center><img src="https://img.shields.io/youtube/views/bnyD3laX_bU?style=social"/></div>

# Have a try
- [Apk](Android/apk/TengineKitDemo-v1.0.3.apk) can be directly downloaded and installed on the phone to see the effect.

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
- face 3dlandmarks
- face attributes for example: age, gender, smile, glasses
- eye iris & landmarks
- body detect
- hand detect(Real-time, not yet on Mobile)
- hand landmarks(Real-time, not yet on Mobile)
- body detect google(Real-time, not yet on Mobile)
- body landamrks(Real-time, not yet on Mobile)

# Update (2021/02/25)
- Update Core v0.0.4
- Update Face v0.0.3
- Update Hand v0.0.2
- Update Body v0.0.3
- Fixed some Bugs
- Rename models
- Linux libTengineKitSDK.so
- Body Detect model

# Performance(Face Detect & Face Landmark)

| CPU | Time consuming | Frame rate |
| :---: | :---: | :---: |
| Kirin 980 | 4ms | 250fps | 
| Qualcomm 855 | 5ms | 200fps |
| Kirin 970 | 7ms | 142fps |
| Qualcomm 835 | 8ms | 125fps |
| Kirin 710F| 9ms | 111fps |
| Qualcomm 439 | 16ms | 62fps |
| MediaTek Helio P60 | 17ms | 59fps |
| Qualcomm 450B | 18ms | 56fps |

# Landmark Points Order
[Landmark Points Order](Docs/POINTORDER.md)

# Contact
About the use of TengineKit and face-related technical exchanges, you can join the following QQ groups(Group Answer:TengineKit):
- TengineKit communication QQ group: 630836519
- Scan to join group
 
 <img width="256" height="256"  src="https://openailab.oss-cn-shenzhen.aliyuncs.com/images/QQGroup_QR.jpg"/>