package com.faceDemo.camera;

import android.hardware.Camera;

import com.faceDemo.activity.CameraActivity;

public class CameraEngine {
    private static CameraEngine instance;
    public static CameraEngine getInstance()
    {
        if (instance == null){
            instance = new CameraEngine();
        }
        return instance;
    }
    public int getCameraOrientation(int orientation) {
        int degrees = 0;
        switch (orientation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 270;
                break;
            case 3:
                degrees = 180;
                break;
        }

        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraActivity.CameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result ) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
