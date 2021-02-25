# *API*
方法位于```com.tenginekit```下。

## 初始化
#### 参数
 - context: activity的context 
 - config: ```AndroidConfig``` 项目的配置       
``` java
    KitCore.init(Context context, AndroidConfig config);
```

比如这样设置默认的配置。
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
> ``` .create()``` 首先创建一个AndroidConfig。

配置模式：    
> ```.setNormalMode()``` 设置为普通模式   
```.setCameraMode()``` 设置为摄像机模式用于（该模式会自动根据前后摄像头旋转图片）     
```.setHandleMode(HandleMode mHandleMode)``` 传参方式设置模式   
```java
    public enum HandleMode{
        Normal, // 普通模式
        Camera // 摄像机模式
    }
```

配置输入图片格式：    
> ```.setDefaultInputImageFormat()``` 设置默认图片格式(YUV_NV21)        
```.setInputImageFormat(ImageFormat imageFormat)``` 传参方式设置输入图片格式    
```java
    public enum ImageFormat
    {
        YUV_NV21,
        RGB,
        RGBA,
        GRAY
    }
```

配置输入输出：    
> ```.setInputImageSize(int w, int h)``` 输入宽高(preview_w, preview_h)   
```.setOutputImageSize()``` 输出宽高(显示大小)  

配置功能(功能都基于检测)：    
> ```.setDefaultFunc()``` 设置默认功能(人脸检测和关键点)  
```.openFunc(Func func)``` 传参设置功能(例如人脸必须设置图片检测```AndroidConfig.Func.Detect```或者```AndroidConfig.Func.BlazeFace```)    
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
| enum | 功能 |
| :---: | :---: |
| Detect | 人脸检测 |
| Landmark | 人脸关键点(212) |
| Attribution | 人脸属性 |
| BlazeFace | 人脸检测 |
| FaceMesh | 人脸3d关键点(468) |
| Iris | 虹膜关键点 |
| BodyDetect | 身体检测 |
| BlazePose | 身体检测 |
| BlazePoseLandmark | 身体关键点 |
| HandDetect | 手部检测 |
| HandLandmark3d | 手部关键点 |

Tips：
 - BlazeFace, FaceMesh, Iris, BlazePose, BlazePoseLandmark, HandDetect, HandLandmark3d,的模型都是来自Google，项目地址为：https://github.com/google/mediapipe
 - 如果你想使用身体关键点，请使用```BlazePose```作为身体检测方式。

## 获取人脸检测信息
由于脸部所有功能都基于人脸检测，所以先创建一个```Face.FaceDetect```的对象。Detect检测会更快，BlazeFace更准确,支持角度更大但是不支持多脸(BlazeFace是基于Google模型的)。最终会返回一个[List<FaceDetectInfo>](#FaceDetectInfo);
#### 参数
 - imageData: 输入数据    
```java
    Face.FaceDetect faceDetect = Face.detect(imageData);
    List<FaceDetectInfo> faceDetectInfos = faceDetect.getDetectInfos();
```

## 获取人脸关键点信息（212关键点）  
人脸关键点功能是基于人脸检测的，所以landmark信息获取方法要基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceLandmarkInfo列表](#FaceLandmarkInfo);
``` java
    List<FaceLandmarkInfo> landmarkInfos = faceDetect.landmark2d();;
```

## 获取3D人脸关键点信息(此功能是基于Google的模型)（468关键点）  
3D人脸关键点功能是基于人脸检测的，所以landmark3d信息获取方法要基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceLandmark3dInfo列表](#FaceLandmark3dInfo);
``` java
    List<FaceLandmark3dInfo> landmarkInfos = faceDetect.landmark3d();;
```

## 获取人脸属性信息
属性功能是基于人脸检测的，所以attribution信息获取方法是基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceAttributionInfo列表](#FaceAttributionInfo);
```java
    List<FaceAttributionInfo> attributionInfos = faceDetect.attribution();
```

## 获取获取虹膜信息(此功能是基于Google的模型)(76关键点)  
虹膜功能是基于人脸检测和人脸3d关键点，所以iris3d信息获取要基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceIrisInfo列表](#FaceIrisInfo);76个关键点包含5个虹膜关键点以及71个眼睛周围关键点。
``` java
    List<FaceIrisInfo> irisInfos = faceDetect.iris3d();;
```

## 获取手部检测信息
由于手部所有功能都基于人脸检测，所以先创建一个```Hand.HandDetect```的对象。最终会返回一个[List<HandDetectInfo>](#HandDetectInfo);
#### 参数
 - imageData: 输入数据    
```java
    Hand.HandDetect handDetect = Hand.detect(imageData);
    List<HandDetectInfo> handDetectInfos = handDetect.getDetectInfos();
```

## 获取手部关键点信息（21关键点）  
手部关键点功能是基于手部检测的，所以landmark信息获取方法要基于前面创建的```Hand.HandDetect```对象。最终返回一个[HandLandmarkInfo](#HandLandmarkInfo);
``` java
    List<HandLandmarkInfo> landmarkInfos = handDetect.landmark3d();;
```

## 获取身体检测信息
由于身体所有功能都基于人脸检测，所以先创建一个```Body.BodyDetect```的对象。最终会返回一个[List<BodyDetectInfo>](#BodyDetectInfo);
#### 参数
 - imageData: 输入数据    
```java
    Body.BodyDetect bodyDetect = Body.detect(imageData);
    List<BodyDetectInfo> bodyDetectInfos = bodyDetect.getDetectInfos();
```

## 获取身体关键点信息（25关键点）  
身体关键点功能是基于身体检测的，所以landmark信息获取方法要基于前面创建的```Body.BodyDetect```对象。最终返回一个[BodyLandmarkInfo](#BodyLandmarkInfo);
``` java
    List<BodyLandmarkInfo> landmarkInfos = bodyDetect.landmark2d();;
```

## 切换摄像头
切换摄像头，只有处于摄像机模式时才有用。
#### 参数
 - back：是否是后置摄像头。 
``` java
    Face.Camera.switchCamera(boolean back); 
```

## 设置旋转
设置旋转，只有在摄像机模式时才有用。
#### 参数
 - ori: 旋转角度(以竖直为0°)
 - isScreenRotate: 是否展示区域跟随旋转
 - outputW: 展示区域宽度
 - outputH: 展示区域高度        
```java
    Face.Camera.setRotation(int ori, boolean isScreenRotate, int outputW, int outputH);
```

## 释放
``` java
    FaceManager.getInstance().release();
```

# 数据结构   
## FaceDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | 距离显示上边缘距离 |
| bottom | int | 距离显示下边缘距离 |
| left | int | 距离显示左边缘距离 |
| right | int | 距离显示右边缘距离 |
| width | int | 人脸框的宽 |
| height | int | 人脸框的高 |           

```.asRect()```是FaceDetectInfo内的方法，可以直接返回一个```Rect```类的对象。

## FaceLandmarkInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | 人脸关键点信息 212个点 |
| pitch | float | 人脸pitch方向转角 |
| yaw | float | 人脸yaw方向转角 |
| roll | float | 人脸roll方向转角 |
| leftEyeClose | float | 左眼闭合置信度  0~1 |
| rightEyeClose | float | 右眼闭合置信度  0~1 |
| mouseClose | float | 嘴巴闭合置信度  0~1 |
| mouseOpenBig | float | 嘴巴张大置信度  0~1 |

## FaceLandmark3dInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | 人脸关键点信息 468个点 |

## FaceAttributionInfo  
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| age | int | 年龄 |
| isMan | boolean | 是否是男性 |
| smile | int | 微笑程度0~100 |
| glasses | boolean | 是否佩戴眼镜 |
| beatyOfManLook | int | 男性角度看的颜值 |
| beautyOfWomanLook | int | 女性角度看的颜值 |

## FaceIrisInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| leftEye | eyeInfo(#eyeInfo) | 左眼信息 |
| rightEye | eyeInfo(#eyeInfo) | 右眼信息 |

## eyeInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| eyeLandmark | List<TenginekitPoint> | 眼部71个关键点 |
| eyeIris | List<TenginekitPoint> | 虹膜的5个关键点 |

## HandDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | 距离显示上边缘距离 |
| bottom | int | 距离显示下边缘距离 |
| left | int | 距离显示左边缘距离 |
| right | int | 距离显示右边缘距离 |
| width | int | 手框的宽 |
| height | int | 手框的高 | 

## HandLandmarkInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | 手部关键点信息 21个关键点 |

## BodyDetectInfo   
| Parameter name | Parameter type | Comment |
| :---: | :---: | :---: |
| top | int | 距离显示上边缘距离 |
| bottom | int | 距离显示下边缘距离 |
| left | int | 距离显示左边缘距离 |
| right | int | 距离显示右边缘距离 |
| width | int | 身体框的宽 |
| height | int | 身体框的高 | 

## BodyLandmarkInfo 
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| landmarks | List<TenginekitPoint> | 身体关键点信息 25个关键点 |

## TenginekitPoint    
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| x | float | 点的x位置信息 |
| y | float | 点的y位置信息 |

## 图像处理文档 
### 参数
- bitmap : Android图片
- inputX1 : 输入图片的左上角x
- inputY1 : 输入图片的左上角y
- inputX2 : 输入图片的右下角x
- inputY2 : 输入图片的右下角y
- ouputW : 输出图片的宽
- outputH : 输出图片的高
- rotation : 旋转角
- mirror : 是否镜像         
```java
    synchronized static public Bitmap convertImage(Bitmap bitmap,
                                    int inputX1, int inputY1, int inputX2, int inputY2,
                                    int outputW, int outputH, int rotation, boolean mirror);
```     

### 参数
- data : 输入图片byte数据
- imageOutputFormat : 输出图片格式
- ouputW : 输出图片的宽
- outputH : 输出图片的高
- rotation : 旋转角
- mirror : 是否镜像     
```java
    synchronized static public byte[] convertCameraYUVData(byte[] data,
                                    AndroidConfig.ImageFormat imageOutputFormat,
                                    int inputW, int inputH,
                                    int outputW, int outputH, int rotation, boolean mirror
```     

### 参数
- data : 输入图片byte数据
- inputW : 输入图片的宽
- inputH : 输入图片的高
- ouputW : 输出图片的宽
- outputH : 输出图片的高
- rotation : 旋转角
- mirror : 是否镜像     
```java
    synchronized static public Bitmap convertCameraYUVData(byte[] data,
                                    int inputW, int inputH,
                                    int outputW, int outputH,
                                    int rotation, boolean mirror
```     

### 参数
- data : 输入图片byte数据
- imageInputFormat : 输入图片格式
- imageOutputFormat : 输出图片格式
- inputW : 输入图片的宽
- inputH : 输入图片的高
- inputX1 : 输入图片的左上角x
- inputY1 : 输入图片的左上角y
- inputX2 : 输入图片的右下角x
- inputY2 : 输入图片的右下角y
- ouputW : 输出图片的宽
- outputH : 输出图片的高
- rotation : 旋转角
- mirror : 是否镜像     
```java
    synchronized static public byte[] convertImage(byte[] data,
                                    AndroidConfig.ImageFormat imageInputFormat, AndroidConfig.ImageFormat imageOutputFormat,
                                    int inputW, int inputH,
                                    int inputX1, int inputY1, int inputX2, int inputY2,
                                    int outputW, int outputH, int rotation, boolean mirror
```     