package com.tenginekit.tenginedemo.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.tenginekit.tenginedemo.Constant;

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static final String TAG = Constant.LOG_TAG;
    private CameraV2Renderer mCameraV2Renderer;

    public CameraV2GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(CameraV2Manager camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(3);

        mCameraV2Renderer = new CameraV2Renderer();
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context);

        setRenderer(mCameraV2Renderer);
    }

    public CameraV2GLSurfaceView(Context context) {
        this(context, null);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mCameraV2Renderer.getSurfaceTextrue();
    }

    public void updateSegRes(Bitmap res) {
        mCameraV2Renderer.updateSegRes(res);
    }
}
