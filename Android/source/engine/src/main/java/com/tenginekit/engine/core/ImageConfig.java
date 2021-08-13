package com.tenginekit.engine.core;

public class ImageConfig {
    public int degree;
    public FaceImageFormat format;
    public int width;
    public int height;
    public byte[] data;
    public boolean mirror;

    public enum FaceImageFormat {
        YUV(0),
        RGB(1),
        RGBA(2);
        public final int value;

        FaceImageFormat(int value) {
            this.value = value;
        }
    }

    public ImageConfig() {
    }


    public ImageConfig setDegree(int degree) {
        this.degree = degree;
        return this;
    }

    public ImageConfig setFormat(FaceImageFormat format) {
        this.format = format;
        return this;
    }

    public ImageConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    public ImageConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    public ImageConfig setData(byte[] data) {
        this.data = data;
        return this;
    }

    public ImageConfig setMirror(boolean mirror) {
        this.mirror = mirror;
        return this;
    }
}
