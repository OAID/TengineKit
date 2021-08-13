package com.tenginekit.engine.common;

public class TenginekitPoint {
    public float X;
    public float Y;

    public TenginekitPoint(float X, float Y) {
        this.X = X;
        this.Y = Y;
    }

    public TenginekitPoint rotateByOrientation(int orientation, float width, float height) {
        float tmp;
        switch (orientation) {
            case 1:
                tmp = Y;
                Y = X * height;
                X = (1 - tmp) * width;
                break;
            case 2:
                tmp = X;
                X = Y * width;
                Y = (1 - tmp) * height;
                break;
            case 3:
                X = (1 - X) * width;
                Y = (1 - Y) * height;
            case 0:
                X *= width;
                Y *= height;
                break;
        }
        return this;
    }
}
