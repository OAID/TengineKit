package com.tenginekit.tenginedemo.camera;

import android.graphics.Rect;
import android.hardware.Camera;

import com.tenginekit.tenginedemo.CameraActivity;

public class CameraEngine {
    private static CameraEngine instance;

    public static CameraEngine getInstance() {
        if (instance == null) {
            instance = new CameraEngine();
        }
        return instance;
    }


    public static Rect getRectByOrientation(float x1, float y1, float x2, float y2, float width, float height, int degree) {
        float left = x1;
        float top = y1;
        float right = x2;
        float bottom = y2;
        switch (degree) {
            case 1:
                left = 1 - y2;
                top = x1;
                right = 1 - y1;
                bottom = x2;
                break;
            case 2:
                left = y1;
                top = 1 - x2;
                right = y2;
                bottom = 1 - x1;
                break;
            case 3:
                left = 1 - x1;
                top = 1 - y1;
                right = 1 - x2;
                bottom = 1 - y2;
                break;
            case 0:
                break;
        }
        return new Rect((int) (left * width), (int) (top * height), (int) (right * width), (int) (bottom * height));
    }


    public int getRotateDegree(int orientation) {
        int degree = 0;
        switch (orientation) {
            case 0:
                degree = 270;
                break;
            case 1:
                degree = 0;
                break;
            case 2:
                degree = 180;
                break;
            case 3:
                degree = 90;
                break;
        }
        return degree;
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
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
