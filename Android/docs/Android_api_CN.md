# *API*
方法位于```com.tenginekit```下。

## 初始化
 - path: ```model``` 代表模型地址   

## SdkConfig
- sdkFunction: 函数枚举，现在只支持FACE


``` kotlin
	val sdkConfig = SdkConfig().apply {
        sdkFunction = SdkConfig.SdkFunction.FACE
    }
    TengineKitSdk.getInstance().initSdk(path, config)
```

## 获取人脸检测信息

我们提供统一的配置函数接口

```
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

### 脸部识别数据

所有检测值都做从0到1归一化处理

* x1: 距离显示左边缘距离
* y1: 距离显示上边缘距离
* x2: 距离显示右边缘距离
* y2: 距离显示下边缘距离
* landmark: 如果不为空，则包含212个人面检测关键点


## 释放
```
	TengineKitSdk.getInstance().release()
```

 