# *API*
The located Function under ```com.tenginekit```.
## Init

- path: ```model``` is the model file path

### SdkConfig

- sdkFunction: function enum, only support FACE now


``` kotlin
	val sdkConfig = SdkConfig().apply {
        sdkFunction = SdkConfig.SdkFunction.FACE
    }
    TengineKitSdk.getInstance().initSdk(path, config)
```


## Get Face Detect Information
We merge all the functions into one function

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

### FaceConfig
* landmark2d(boolean): set true if need landmark2d except detect face rect
* video(boolean): set true if in camera mode

### ImageConfig
* data(byte[]): set image data byte array of image raw data
* degree(int): set rotate degree need if in camera mode, need to rotate the right angle to detect the face
* width(int): set bitmap width or preview width
* height(int): set bitmap height or preview height
* format(enum ImageConfig.FaceImageFormat): set image format, support RGB format and NV21 format current now

### Face

all detect values ​​are normalized from 0 to 1

* x1: face rect left
* y1: face rect top
* x2: face rect right
* y2: face rect bottom
* landmark: if not null landmark contain 212 face key points


## Release
```
	TengineKitSdk.getInstance().release()
```
