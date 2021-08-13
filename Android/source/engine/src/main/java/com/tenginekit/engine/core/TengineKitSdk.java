package com.tenginekit.engine.core;

import android.util.Log;

import com.tenginekit.engine.SDKConstant;
import com.tenginekit.engine.face.Face;
import com.tenginekit.engine.face.FaceConfig;

public class TengineKitSdk {
    private TengineKitSdk() {

    }

    private static TengineKitSdk instance;
    private TengineKitEngine tengineKitEngine;
    private boolean hasInit = false;

    public static TengineKitSdk getInstance() {
        if (instance == null) {
            instance = new TengineKitSdk();
        }
        return instance;
    }

    public synchronized void initSdk(String modelPath, SdkConfig config) {
        if (hasInit && tengineKitEngine != null) {
            Log.i(SDKConstant.SDK_LOG_TAG, "hasInited before");
            return;
        }
        Log.i(SDKConstant.SDK_LOG_TAG, "initSdk start");
        tengineKitEngine = new TengineKitEngine(modelPath, config);
        Log.i(SDKConstant.SDK_LOG_TAG, "initSdk end");
        hasInit = true;
    }

    public synchronized void release() {
        if (tengineKitEngine != null) {
            tengineKitEngine.release();
            hasInit = false;
        }
    }

    public Face[] detectFace(ImageConfig imageConfig, FaceConfig faceConfig) {
        if (!hasInit) {
            Log.e(SDKConstant.SDK_LOG_TAG, "please TengineAiSdk.initSDK() before detect");
            return null;
        }
        return tengineKitEngine.detectFace(imageConfig, faceConfig);
    }

}
