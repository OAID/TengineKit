package com.tenginekit.engine.core;

import com.tenginekit.engine.face.Face;
import com.tenginekit.engine.face.FaceConfig;

public class TengineKitEngine {

    //存指针
    private long mJniHandler = 0;

    static {
        System.loadLibrary("tenginekit_engine");
    }

    public TengineKitEngine(String path, SdkConfig config) {
        init(path, config);
    }

    public native void init(String modelPath, SdkConfig config);

    public native void release();

    public Face[] detectFace(ImageConfig imageConfig, FaceConfig faceConfig) {
        return nativeDetectFace(imageConfig, faceConfig);
    }

    public synchronized native Face[] nativeDetectFace(ImageConfig imageConfig, FaceConfig faceConfig);
}
