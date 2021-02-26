# *API*
The located Function under ```com.tenginekit```.
## init
#### Parameter
 - context: context in activity，
 - config: ```AndroidConfig``` is the config of work

``` java
    KitCore.init(Context context, AndroidConfig config);
```

For example, set the default configuration like this.
``` java
    KitCore.init(context,
            AndroidConfig
                .create()
                .setCameraMode()
                .setDefaultFunc()
                .setDefaultInputImageFormat()
                .setInputImageSize(previewWidth, previewHeight)
                .setOutputImageSize(OutputWidth, OutputHeight)
        );
``` 

## AndroidConfig
> ``` .create()``` First create an AndroidConfig.

Configuration mode:    
> ```.setNormalMode()``` Set to normal mode   
```.setCameraMode()``` Set to camera mode(This mode will automatically rotate the picture according to the front and rear camera)     
```.setHandleMode(HandleMode mHandleMode)``` Parameter transmission mode setting mode   
```java
    public enum HandleMode{
        Normal, // Normal mode
        Camera // Camera mode
    }
``` 

Configure input picture format:   
> ```.setDefaultInputImageFormat()``` Set the default picture format (YUV_NV21)  
```.setInputImageFormat(ImageFormat imageFormat)``` Parameter transmission mode to set input picture format    
```java
    public enum ImageFormat
    {
        YUV_NV21,
        RGB,
        RGBA,
        GRAY
    }
```

Configure input and output:    
> ```.setInputImageSize(int w, int h)``` Input width and height (preview_w, preview_h)   
```.setOutputImageSize()``` Output width and height (display size)      

Configuration function (functions are based on image detection):    
> ```.setDefaultFunc()``` Set default functions (face detection and key points)  
```.openFunc(Func func)``` Pass parameter setting function (image detection must be set to ```AndroidConfig.Func.Detect``` or ```AndroidConfig.Func.BlazeFace```)    
```java
    public enum Func
    {
        Detect, 
        Landmark,
        Attribution,
        BlazeFace,
        FaceMesh,
        Iris,
        BodyDetect,
        BlazePose,
        BlazePoseLandmark,
        HandDetect,
        HandLandmark3d,
    }
```
| enum | Function |
| :---: | :---: |
| Detect | Face detect |
| Landmark | Face landmarks(212) |
| Attribution | Face attribution |
| BlazeFace | Face detect |
| FaceMesh | Face 3d landmarks(468) |
| Iris | Iris landmarks |
| BodyDetect | Body detect |
| BlazePose | Body detect |
| BlazePoseLandmark | Body landmarks |
| HandDetect | Hand detect |
| HandLandmark3d | Hand landmarks | 

Tips：
 - BlazeFace, FaceMesh, Iris, BlazePose, BlazePoseLandmark, HandDetect,  HandLandmark3d, models are from Google，The project address is：https://github.com/google/mediapipe
 - If you want to use body landmark , please use ```BlazePose``` as body detect.

## Get Face Detect Infomation     
Since all functions on face are based on face detection, first create an object of ```Face.FaceDetect```. Detect detection will be faster, BlazeFace will be more accurate and the angle can be larger , but it didn`t support much faces(BlazeFace is based on the Google model). Will eventually return a [FaceDetectInfo list](#FaceDetectInfo);
#### Parameter
 - imageData: Input data

```java
    Face.FaceDetect faceDetect = Face.detect(imageData);
    List<FaceDetectInfo> faceDetectInfos = faceDetect.getDetectInfos();
```

## Get Landmark Infomation  
The face key point function is based on face detection, so the landmark information acquisition method should be based on the ```Face.FaceDetect``` object created earlier. Finally returns a FaceLandmarkInfo list.
``` java
    List<LandmarkInfo> landmarkInfos = faceDetect.landmark2d();
```
Tips:
 - 212 landmarks, landmark order [Click Here](../../Docs/POINTORDER.md).
 - FaceLandmarkInfo list data struct [Click Here](#FaceLandmarkInfo).

## Get 3D Landmark Infomation 
The face key point function is based on face detection, so the 3d landmark information acquisition method should be based on the ```Face.FaceDetect``` object created earlier. Finally returns a FaceLandmark3dInfo list.
``` java
    List<FaceLandmark3dInfo> landmarkInfos = faceDetect.landmark3d();
```
Tips:
 - This feature is based on Google model.
 - 468 landmarks.
 - FaceLandmark3dInfo list data struct [Click Here](#FaceLandmark3dInfo).

## Get Attribution Infomation
The attribute function is based on face detection, so the method of obtaining attribute information is based on the ```Face.FaceDetect``` object created earlier. Finally returns a FaceAttributionInfo list.
```java
    List<AttributionInfo> attributionInfos = faceDetect.attribution();
```
Tips:
 - FaceAttributionInfo list content data struct [Click Here](#FaceAttributionInfo).

## Get Iris Infomation
The iris function is based on face detection and face 3d key points, so iris3d information acquisition should be based on the ```Face.FaceDetect``` object created earlier. Finally return a FaceIrisInfo list. 
``` java
    List<FaceIrisInfo> irisInfos = faceDetect.iris3d();;
```
Tips:
 - This function is based on Google's model.
 - FaceIrisInfo list data struck [Click Here](#FaceIrisInfo);
 - 76 key points include 5 iris key points and 71 key points around the eyes.

## Get Hand Detection Information()
Since all the functions of the hand are based on hand detection, first create an object of ```Hand.HandDetect```. Will eventually return a HandDetectInfo list.
#### Parameters
- imageData: input data
```java
    Hand.HandDetect handDetect = Hand.detect(imageData);
    List<HandDetectInfo> handDetectInfos = handDetect.getDetectInfos();
```
Tips:
 - This function is based on Google's model.
 - HandDetectInfo list data struct [Click Here](#HandDetectInfo).

## Get Hand Landamrk Infomation
The hand key point function is based on hand detection, so the landmark information acquisition method should be based on the ```Hand.HandDetect``` object created earlier. Finally returns a HandLandmarkInfo list.
``` java
     List<HandLandmarkInfo> landmarkInfos = handDetect.landmark3d();
```
Tips:
 - This function is based on Google's model.
 - 21 key points.
 - HandLandmarkInfo data struct [Click Here](#HandLandmarkInfo).

## Get Body Detection Information
Since all the functions of the body are based on body detection, first create an object of ```Body.BodyDetect```. Will eventually return a BodyDetectInfo list.
#### Parameters
- imageData: input data
```java
    Body.BodyDetect bodyDetect = Body.detect(imageData);
    List<BodyDetectInfo> bodyDetectInfos = bodyDetect.getDetectInfos();
```
Tips:
- ```BlazePose``` function is based on Google's model.
- ```BodyDetect``` is a faster function , but can`t use Body Landmark.
- BodyDetectInfo data struct [Click Here](#BodyDetectInfo).

## Get Body Landamrk Infomation
The body key point function is based on body detection, so the landmark information acquisition method should be based on the ```Body.BodyDetect``` object created earlier. Finally returns a BodyLandmarkInfo list.
``` java
     List<BodyLandmarkInfo> landmarkInfos = bodyDetect.landmark2d();
```
Tips:
 - This function is based on Google's model.
 - 25 key points.
 - BodyLandmarkInfo list data struct [Click Here](#BodyLandmarkInfo).

## Wwitch Camera
Switching cameras is only useful when in camera mode.
#### Parameter
 - back:Whether it is a rear camera

``` java
    Face.Camera.switchCamera(boolean back); 
```

## Set Rotation
Setting rotation is only useful in camera mode.
#### Parameter
 - ori: Rotation angle (vertical to 0°)
 - isScreenRotate: whether the display area follows rotation
 - outputW: show width
 - outputH: show height     
```java
    Face.Camera.setRotation(int ori, boolean isScreenRotate, int outputW, int outputH);
```

## Release
``` java
    FaceManager.getInstance().release();
```

## Data structure       
#### FaceDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | Distance display upper edge distance |
| bottom | int | Distance display bottom edge distance |
| left | int | Distance shows the distance of the left edge |
| right | int | Distance display right edge distance |
| width | int | Face rect width |
| height | int | Face rect height |         

```.asRect()``` is a method in FaceDetectInfo, which can directly return an object of class ```Rect```.

#### FaceLandmarkInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | Face key point information 212 points |
| pitch | float | Human face pitch direction corner |
| yaw | float | Human face yaw direction corner |
| roll | float | Human face roll direction corner |
| leftEyeClose | float | Left eye closure confidence  0~1 |
| rightEyeClose | float | Right eye closure confidence  0~1 |
| mouseClose | float | Mouth closure confidence  0~1 |
| mouseOpenBig | float | Open mouth Big confidence  0~1 |

#### FaceLandmark3dInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | Face key point information 468 points |

#### FaceAttributionInfo  
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| age | int | Age |
| isMan | boolean | Is it male |
| smile | int | Smile degree0~100 |
| glasses | boolean | Whether to wear glasses |
| beatyOfManLook | int | Face value from a male perspective |
| beautyOfWomanLook | int | Face value from a female perspective |

#### FaceIrisInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| leftEye | eyeInfo | left eye info |
| rightEye | eyeInfo | right eye info |

#### eyeInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| eyeLandmark | List<TenginekitPoint> | 71 key points of the eye |
| eyeIris | List<TenginekitPoint> | 5 key points of the iris |

#### HandDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | Distance display upper edge distance |
| bottom | int | Distance display bottom edge distance |
| left | int | Distance shows the distance of the left edge |
| right | int | Distance display right edge distance |
| width | int | Hand rect width |
| height | int | Hand rect height |  

#### HandLandmarkInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | Hand key point information 21 points |

#### BodyDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | Distance display upper edge distance |
| bottom | int | Distance display bottom edge distance |
| left | int | Distance shows the distance of the left edge |
| right | int | Distance display right edge distance |
| width | int | Body rect width |
| height | int | Body rect height |  

#### BodyLandmarkInfo 
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | Body key point information 25 points |

#### TenginekitPoint    
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| x | float | Point x position |
| y | float | Point y position |

## ImageHandleApi
### Parameter   
- bitmap: Android picture
- inputX1: input the upper left corner of the picture x
- inputY1: input image in the upper left corner y
- inputX2: Enter the bottom right corner of the picture x
- inputY2: Enter the lower right corner of the picture y
- ouputW: width of output picture
- outputH: the height of the output picture
- rotation: rotation angle
- mirror: whether to mirror

```java
    synchronized static public Bitmap convertImage(Bitmap bitmap,
                                    int inputX1, int inputY1, int inputX2, int inputY2,
                                    int outputW, int outputH, int rotation, boolean mirror);
```     

### Parameter
- data: input picture byte data
- imageOutputFormat: output image format
- ouputW: width of output picture
- outputH: the height of the output picture
- rotation: rotation angle
- mirror: whether to mirror     
```java
    synchronized static public byte[] convertCameraYUVData(byte[] data,
                                    AndroidConfig.ImageFormat imageOutputFormat,
                                    int inputW, int inputH,
                                    int outputW, int outputH, int rotation, boolean mirror
```     

### Parameter
- data: input picture byte data
- inputW: input picture width
- inputH: input image height
- ouputW: width of output picture
- outputH: the height of the output picture
- rotation: rotation angle
- mirror: whether to mirror     
```java
    synchronized static public Bitmap convertCameraYUVData(byte[] data,
                                    int inputW, int inputH,
                                    int outputW, int outputH,
                                    int rotation, boolean mirror
```     

### Parameter
- data: input picture byte data
- imageInputFormat: input image format
- imageOutputFormat: output image format
- inputW: input picture width
- inputH: input image height
- inputX1: input the upper left corner of the picture x
- inputY1: input image in the upper left corner y
- inputX2: Enter the bottom right corner of the picture x
- inputY2: Enter the lower right corner of the picture y
- ouputW: width of output picture
- outputH: the height of the output picture
- rotation: rotation angle
- mirror: whether to mirror     
```java
    synchronized static public byte[] convertImage(byte[] data,
                                    AndroidConfig.ImageFormat imageInputFormat, AndroidConfig.ImageFormat imageOutputFormat,
                                    int inputW, int inputH,
                                    int inputX1, int inputY1, int inputX2, int inputY2,
                                    int outputW, int outputH, int rotation, boolean mirror
```     