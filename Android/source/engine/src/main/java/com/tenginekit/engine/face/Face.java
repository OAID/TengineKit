package com.tenginekit.engine.face;

public class Face {
    public float x1, y1, x2, y2;
    public float[] landmark;
    public float headX, headY, headZ, leftEyeClose, rightEyeClose, mouthClose, mouthBigOpen;
    public float[] landmark3d;

    @Override
    public String toString() {
        return "Face{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                '}';
    }
}
