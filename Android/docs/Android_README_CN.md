# 配置Gradle
Project中的build.gradle添加
```java
    repositories {
        ...
        jcenter()
        mavenCentral()
        ...
    }

    allprojects {
        repositories {
            ...
            jcenter()
            mavenCentral()
            ...    
        }
    }
    
```           
主Module中的```build.gradle```添加,选择你需要的功能。```core```是必须的。
```java
    dependencies {
        ...
        // 必须有，并且必须是最新的
        implementation 'com.tengine.tenginekit:core:0.0.4'
        // Function on Face
        implementation 'com.tengine.tenginekit:face:0.0.3'
        // Function on Hand
        implementation 'com.tengine.tenginekit:hand:0.0.2'
        // Function on Body
        implementation 'com.tengine.tenginekit:body:0.0.3'
        ...
    } 
```
# 系统
Android
- 系统最低要求API 19

# 使用
- [API](Android_api_CN.md) : 使用Tengine-Kit API来完成你所需要得功能。
- [使用示例](Usage.md) : 您可以伴随着您给我们的```star```一起看看使用示例，感谢。

# 权限
``` permission
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<uses-permission android:name="android.permission.CAMERA"/>
```

# 示例
“sample/”文件夹下的Android代码。
# 接入指南
在TengineKit Api的setRotation 里有两个有两个参数ori和is_screen_rotate，分别为旋转角度和是否跟随屏幕旋转。这个是否跟随屏幕在Manifest里面的android:screenOrientation参数可以设置。不设置这个参数就是跟随屏幕旋转。
# 处理过程
## 1.设备预览
这部分是从Camera获取数据，作为SDK的输入。
## 2.角度
我们以竖直屏幕下为0度角，由于Android摄像头采集的数据实际总是偏差90，所以在设置ori参数的时候都需要 + (-90)，安卓的实际旋转角是需要通过传感器加上函数计算去获得。具体可以看Demo项目中的示例。
## 3.渲染
在渲染的时候是以0°角去渲染，是人在正常情况下看到正常输出。Android部分有Canvas和Opengl渲染，选用Opengl渲染可以让你的apk效果更好。

# 图像处理
[图像处理](Android_api_CN.md#图像处理文档)进行缩放、旋转、裁剪、改变图片格式。