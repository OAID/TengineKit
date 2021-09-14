# *API*
方法位于```com.tenginekit```下。

## 1.初始化
   
``` kotlin
	val sdkConfig = SdkConfig().apply {
		sdkFunction = SdkConfig.SdkFunction.FACE  //run face detect service
		//sdkFunction = SdkConfig.SdkFunction.SEG //run seg human service  
	}
	TengineKitSdk.getInstance().initSdk(path, config, context)
```
### Sdk设置

- path: ```model``` 代表模型地址
 
- sdkFunction: 函数枚举，现在只支持FACE/SEG

- context: android context


## 2.运行AI服务

我们提供统一的配置函数接口

 ``` kotlin
 	val byte = ImageUtils.bitmap2RGB(bitmap)
 	val faceConfig = FaceConfig().apply {
 			detect = true
 			landmark2d = true
 			video = false
 	}
 	val imageConfig = ImageConfig().apply {
 			data = byte
 			degree = 0
 			mirror = false
 			height = bitmapHeight
 			width = bitmapWidth
 			format = ImageConfig.FaceImageFormat.RGB
 	}
 	val faces = TengineKitSdk.getInstance().detectFace(faceConfig, config)
 ```
### 人脸识别设置
* landmark2d(boolean): 如果需要landmark2d功能，则设置为true
* video(boolean): 如果在相机模式下设置为true

### 图像处理设置
* data(byte[]): 输入图片byte数据
* degree(int): 如果在相机模式下，需要设置正确的旋转角度来检测人脸 
* width(int): 设置位图宽度或预览宽度
* height(int): 设置位图高度或预览高度
* format(enum ImageConfig.FaceImageFormat): 设置图片格式，目前支持RGB格式和NV21格式

### 脸部识别

所有检测值都做从0到1归一化处理

* x1: 距离显示左边缘距离
* y1: 距离显示上边缘距离
* x2: 距离显示右边缘距离
* y2: 距离显示下边缘距离
* landmark: 如果不为空，则包含212个人面检测关键点
* headX:  人脸pitch方向转角
* headY:  人脸yaw方向转角
* headZ:  人脸roll方向转角
* leftEyeClose:  左眼闭合置信度 0~1
* rightEyeClose: 右眼闭合置信度 0~1
* mouthClose: 嘴巴闭合置信度 0~1
* mouthBigOpen: 嘴巴张大置信度 0~1

### 人体分割
直接返回mask，mask是一个Android Bitmap，它的宽度是398，高度是224，格式是ARGB_8888

 ``` kotlin
 	val byte = ImageUtils.bitmap2RGB(bitmap)
 	val config = SegConfig()
 	val imageConfig = ImageConfig().apply {
 			data = byte
 			degree = 0
 			mirror = false
 			height = it.height
 			width = it.width
 			format = ImageConfig.FaceImageFormat.RGB
 	}
 	val bitmapMask = TengineKitSdk.getInstance().segHuman(imageConfig, config)
 ```
#### 分割设置
* 当前默认为人像分割配置


## 3.释放
```
	TengineKitSdk.getInstance().release()
```

 