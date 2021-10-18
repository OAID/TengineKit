package com.tenginekit.tenginedemo.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraV2GLSurfaceView extends GLSurfaceView {
    private CameraV2BaseRenderer mCameraV2Renderer;

    public CameraV2GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(CameraV2BaseRenderer renderer) {
        setEGLContextClientVersion(3);
        mCameraV2Renderer = renderer;
        setRenderer(renderer);
    }

    public CameraV2GLSurfaceView(Context context) {
        this(context, null);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mCameraV2Renderer.getSurfaceTexture();
    }
}
