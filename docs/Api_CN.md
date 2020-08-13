# *API*
方法位于```com.tenginekit```下。

## 初始化
#### 参数
 - context: activity的context 
 - config: ```AndroidConfig``` 项目的配置       
``` java
    Face.init(Context context, AndroidConfig config);
```

比如这样设置默认的配置。
``` java
    Face.init(context,
            AndroidConfig
                .create()
                .setCameraMode()
                .setDefaultFunc()
                .setDefaultInputImageFormat()
                .setInputImageSize(previewWidth, previewHeight)
                .setOutputImageSize(OutputWidth, OutputHeight)
        );
```

## AndoridConfig
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

配置功能(功能都基于图像检测)：    
> ```.setDefaultFunc()``` 设置默认功能(人脸检测和关键点)  
```.openFunc(Func func)``` 传参设置功能(必须设置图片检测```AndroidConfig.Func.Detect```或者```AndroidConfig.Func.GoDetect```)    
```java
    public enum Func
    {
        Detect,
        Landmark,
        Attribution,
        Landmark3d,
        GoDetect,
    }
```

## 获取人脸检测信息
由于所有功能都基于人脸检测，所以先创建一个```Face.FaceDetect```的对象。Detect检测会更快，GoDetect会更准并且角度可以更大(GoDetect是基于Google模型的)。最终会返回一个[List<FaceDetectInfo>](#FaceDetectInfo);
#### 参数
 - imageData: 输入数据    
```java
    Face.FaceDetect faceDetect = Face.detect(imageData);
    List<FaceDetectInfo> faceDetectInfos = faceDetect.getDetectInfos();
```

## 获取人脸关键点信息  
人脸关键点功能是基于人脸检测的，所以landmark信息获取方法要基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceLandmarkInfo列表](#FaceLandmarkInfo)
``` java
    List<FaceLandmarkInfo> landmarkInfos = faceDetect.landmark2d();;
```

## 获取3D人脸关键点信息(此功能是基于Google的模型进行的)  
3D人脸关键点功能是基于人脸检测的，所以landmark3d信息获取方法要基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceLandmark3dInfo列表](#FaceLandmark3dInfo)
``` java
    List<FaceLandmark3dInfo> landmarkInfos = faceDetect.landmark3d();;
```

## 获取人脸属性信息
属性功能是基于人脸检测的，所以attribution信息获取方法是基于前面创建的```Face.FaceDetect```对象。最终返回一个[FaceAttributionInfo列表](#FaceAttributionInfo)
```java
    List<FaceAttributionInfo> attributionInfos = faceDetect.attribution();
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
| landmarks | List<FaceLandmarkPoint> | 人脸关键点信息 212个点 |
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
| landmarks | List<FaceLandmark3dInfo> | 人脸关键点信息 468个点 |

## FaceAttributionInfo  
| 参数名 | 参数类型 | 注释 |
| :---: | :---: | :---: |
| age | int | 年龄 |
| isMan | boolean | 是否是男性 |
| smile | int | 微笑程度0~100 |
| glasses | boolean | 是否佩戴眼镜 |
| beatyOfManLook | int | 男性角度看的颜值 |
| beautyOfWomanLook | int | 女性角度看的颜值 |

## FaceLandmarkPoint    
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