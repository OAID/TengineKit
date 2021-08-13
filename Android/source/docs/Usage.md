# Usage

You can use Tengine Kit to detect faces in images and video.

You can first give the repository a star, your star is the driving force of our efforts, this SDK is definitely your star.


## 0.Before you begin

first download tengine-kit-sdk1.0.0.aar

Then ```build.gradle``` in Main Module add aar dependency
```groovy
    dependencies {
        ...
        implementation files('path/tengine-kit-sdk1.0.0.aar')
        ...
    }
```

## 1.Configure the face detector 

Before you apply face detection to an image, you can change any of the face detector's default settings with a **FaceConfig** object. You can change the following settings:

| Settings |  |
| :------| :------ |
| detect | 	Whether or not to detect faces |
| landmark2d | Whether to attempt to identify facial "landmarks": eyes, ears, nose,mouth, and so on. |
| video | Whether or not use camera mode, image process is specially optimized in camera mode  |

### Images

``` kotlin
    val config = FaceConfig().apply {
        detect = true
        landmark2d = true
        video = false
    }
```

### Video
    
``` kotlin
    val config = FaceConfig().apply {
        detect = true
        landmark2d = true
        video = true
    }
```

## 2.Prepare the input image

You can change the input image settings with a **ImageConfig** object. You can change the following settings:
| Settings |  |
| :------| :------ |
| data | 	Set image data byte array of image raw data |
| degree | Set rotate degree need if in camera mode, need to rotate the right angle to detect the face |
| height | 	Set bitmap height or preview height. |
| width | Set bitmap width or preview width. |
| format | Set image format, support RGB format and NV21 format current now.  |
### Images

``` kotlin
    val byte = ImageUtils.bitmap2RGB(bitmap)
    val imageConfig = ImageConfig().apply {
        data = byte
        degree = 0
        mirror = false
        height = bitmapHeight
        width = bitmapWidth
        format = ImageConfig.FaceImageFormat.RGB
    }
```

### Video

``` kotlin
    val imageConfig = ImageConfig().apply {
        data = mNV21Bytes
        degree = rotateDegree
        mirror = true
        height = previewHeight
        width = previewWidth
        format = ImageConfig.FaceImageFormat.YUV
    }
```

## 3.Get an instance of FaceDetector and Process the image

``` kotlin
    val faces = TengineKitSdk.getInstance().detectFace(imageConfig, config)
```

## 4.Get information about detected faces

Each Face object represents a face that was detected in the image. For each face, you can get its bounding coordinates in the input image,
as well as any other information you configured the face detector to find. For example:

``` kotlin
    if (faces.isNotEmpty()) {
        val faceRects = arrayOfNulls<Rect>(faces.size)
        val faceLandmarks: MutableList<List<TenginekitPoint>> =
    ArrayList<List<TenginekitPoint>>()
        for ((i, face) in faces.withIndex()) {
    val faceLandmarkList = mutableListOf<TenginekitPoint>()
    for (j in 0..211) {
        faceLandmarkList.add(
        j,
        TenginekitPoint(
    face.landmark[j * 2] * width,
    face.landmark[j * 2 + 1] * height
        )
        )
    }
    val rect = Rect(
        (face.x1 * width).toInt(),
        (face.y1 * height).toInt(),
        (face.x2 * width).toInt(),
        (face.y2 * height).toInt()
    )
    faceLandmarks.add(i, faceLandmarkList)
    faceRects[i] = rect
        }

    }
```

