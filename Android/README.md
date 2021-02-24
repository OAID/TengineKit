[中文版本](docs/Android_README_CN.md)

# Gradle Configure
The ```build.gradle``` in Project add
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
The ```build.gradle``` in Main Module add,select which function you need.But the ```core``` is must.
```java
    dependencies {
        ...
        // Must have and must be the newest!!!!
        implementation 'com.tengine.tenginekit:core:0.0.3'
        // Function on Face
        implementation 'com.tengine.tenginekit:face:0.0.3'
        // Function on Hand
        implementation 'com.tengine.tenginekit:hand:0.0.2'
        // Function on Body
        implementation 'com.tengine.tenginekit:body:0.0.2'
        ...
    }
```     

# System
Android
- Min Sdk Version 19


# Usage
- [API](docs/Android_api.md) : Use API to complete the functions you need.
- [Usage](docs/Usage.md) : Specific usage

# Permission
``` permission
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<uses-permission android:name="android.permission.CAMERA"/>
```


# Sample
Android code under the "sample/" folder.
# Access Guide
In setRotation of TengineKit Api, there are two parameters ori and is_screen_rotate, which are the rotation angle and whether to follow the screen rotation. Whether the android:screenOrientation parameter in the Manifest follows the screen can be set. Not setting this parameter is to follow the screen rotation.
# Process
## 1.Device preview
This part is to get data from Camera, as the SDK input.
## 2.Angle
We use the vertical screen as an angle of 0 degrees. Since the data collected by the Android camera always deviates by 90, it is necessary to set + (-90) when setting the ori parameter. The actual rotation angle of Android is to add a function through the sensor Calculate to get. For details, see the example in the Demo project.
## 3.Rendering
When rendering, it is rendered at an angle of 0°, which is the normal output that people see under normal circumstances. The Android part has Canvas and Opengl rendering. Using Opengl rendering can make your apk better.

# ImageHandle
[ImageHandle](docs/Android_api.md#ImageHandleApi) to do Zoom, rotate, crop, and change picture format