package com.tenginekit.engine.face;

public class FaceConfig {
    public boolean detect = true;
    public boolean landmark2d = false;
    public int maxFaceNum = 100;
    public boolean video = true;


    public FaceConfig setDetect(boolean detect) {
        this.detect = detect;
        return this;
    }

    public FaceConfig setLandmark2d(boolean landmark2d) {
        this.landmark2d = landmark2d;
        return this;
    }

    public FaceConfig setMaxFaceNum(int maxFaceNum) {
        this.maxFaceNum = maxFaceNum;
        return this;
    }

    public FaceConfig setVideo(boolean video) {
        this.video = video;
        return this;
    }
}
