# ***API***
The located Function under ```com.tenginekit```.
## ***1. Init Context***


``` kotlin
	val sdkConfig = SdkConfig()
	TengineKitSdk.getInstance().initSdk(path, config, context)
```

### SdkConfig

- backend: predict backend, cpu default now



## ***2. FaceDetect***
### init
``` kotlin
	TengineKitSdk.getInstance().initFaceDetect()
```

### predict
We merge all the functions into one interface
 
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
 
### release
```kotlin
	TengineKitSdk.getInstance().releaseFaceDetect()
``` 



##  ***3. SegBody***

### init

```
	TengineKitSdk.getInstance().initSegBody()
```
### Predict

directly return a mask, the mask is a android bitmap, the mask's width is 398, the height is 224; the mask's format is ARGB_8888
 
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

### release
```
	TengineKitSdk.getInstance().releaseSegBody()
```

##  ***4. BodyDetect***

### init
``` kotlin
	TengineKitSdk.getInstance().initBodyDetect()
```

### predict
We merge all the functions into one interface
 
 ``` kotlin
 	 val data = ImageUtils.bitmap2RGB(bitmap)
    val imageConfig = ImageConfig().apply {
        this.data = data
        this.format = ImageConfig.FaceImageFormat.RGB
        this.height = it.height
        this.width = it.width
        this.mirror = false
        this.degree = 0
    }
    val bodyConfig = BodyConfig()
    val bodyS = TengineKitSdk.getInstance().bodyDetect(imageConfig, bodyConfig)
 ```
 
### release
```kotlin
	 TengineKitSdk.getInstance().releaseBodyDetect()
``` 


## ***5.Release Context***
```
	TengineKitSdk.getInstance().release()
```

## DataStruct

#### FaceConfig
 * landmark2d(boolean): set true if need landmark2d except detect face rect
 * video(boolean): set true if in camera mode

#### SegConfig
* default portrait segmentation config current

#### ImageConfig
* data(byte[]): set image data byte array of image raw data
* degree(int): set rotate degree need if in camera mode, need to rotate the right angle to detect the face
* width(int): set bitmap width or preview width
* height(int): set bitmap height or preview height
* format(enum ImageConfig.FaceImageFormat): set image format, support RGB format and NV21 format current now

#### Face

all detect values ​​are normalized from 0 to 1

* x1: face rect left
* y1: face rect top
* x2: face rect right
* y2: face rect bottom
* landmark: if not null landmark contain 212 face key points
* headX:  Human face pitch direction corner
* headY:  Human face yaw direction corner
* headZ:  Human face roll direction corner
* leftEyeClose:  Left eye closure confidence 0~1
* rightEyeClose: Right eye closure confidence 0~1
* mouthClose: Mouth closure confidence 0~1
* mouthBigOpen: Open mouth Big confidence 0~1

#### Body

* x1: detect body rect left
* y1: detect body rect top
* x2: detect body rect right
* y2: detect body rect bottom
* landmark: if not null landmark contain 16 body key points




