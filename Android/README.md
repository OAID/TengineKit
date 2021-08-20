English | [简体中文](docs/Android_README_CN.md)

# Introduction
This Tengine Kit app demonstrates how to use and integrate various vision based Tengine Kit features into your Android app.

# Gradle Configure
first download tengine-kit-sdk1.0.0.aar

Then ```build.gradle``` in Main Module add aar dependency
```groovy
    dependencies {
    ...
    implementation files('path/tengine-kit-sdk1.0.0.aar')
    ...
}
```     

# System
Android
- Min Sdk Version 21


# Usage
- [API](docs/Android_api.md) : Use API to complete the functions you need.
- [Usage](docs/Usage.md) : Specific usage

# Permission
``` permission
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
```

# Sample
Android code under the "sample/" folder.
# Access Guide
In setRotation of TengineKit Api, there are two parameters ori and is_screen_rotate, which are the rotation angle and whether to follow the screen rotation. Whether the android:screenOrientation parameter in the Manifest follows the screen can be set. Not setting this parameter is to follow the screen rotation.
# Process
## 1.Device preview
This part is to get data from Camera, as the SDK input.
## 2.Angle
We use the vertical screen as an angle of 0 degrees. The data collected by the camera will deviate from a certain angle. If the data collected by the camera cannot be preprocessed correctly, the face cannot be detected.

### preprocess example

![](image/rotateexample.png)

## 3.Rendering
When rendering, it is rendered at an angle of 0°, which is the normal output that people see under normal circumstances. The Android part has Canvas and Opengl rendering. Using Opengl rendering can make your apk better.
