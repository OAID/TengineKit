package com.tengine.cameratest;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static final String TAG = "Filter_CameraV2GLSurfaceView";
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
        super(context);
    }

    public SurfaceTexture getSurfaceTexture(){
        return mCameraV2Renderer.getSurfaceTextrue();
    }
}
