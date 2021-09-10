package com.tenginekit.tenginedemo.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glViewport;

import com.tengine.cameratest.CameraFilter;

public class CameraV2Renderer implements GLSurfaceView.Renderer {
    public static final String TAG = "Filter_CameraV2Renderer";
    private Context mContext;
    CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    CameraV2Manager mCamera;
    boolean bIsPreviewStarted;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private CameraFilter mFilterEngine;
    private SegFilter mSegFilter;

    public void init(CameraV2GLSurfaceView surfaceView, CameraV2Manager camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mCameraV2GLSurfaceView = surfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e("ShiTouren", "onSurefaceCreated");
        mFilterEngine = new CameraFilter();
        mCamera.setSurfaceCreated(true);
        mCamera.openCamera();
        mSegFilter = new SegFilter();
        //initSurfaceTexture();
        //glGenFramebuffers(1, mFBOIds, 0);
        //glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        mFilterEngine.onSurfaceChanged(width, height);
        mSegFilter.onSurfaceChanged(width, height);


        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }
//        if (!bIsPreviewStarted) {
//            bIsPreviewStarted = initSurfaceTexture();
//            bIsPreviewStarted = true;
//            return;
//        }

        //mFilterEngine.onDrawFrameScreen(transformMatrix);
        int outTexture = mFilterEngine.onDrawFrameBuffer(transformMatrix);
        mSegFilter.setUpstreamTexture(outTexture);
        mSegFilter.onDrawFrameScreen(transformMatrix);


        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        //Log.i(TAG, "onDrawFrame: time: " + t);
    }

    public SurfaceTexture getSurfaceTextrue() {
        mSurfaceTexture = new SurfaceTexture(mFilterEngine.getOESTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mCameraV2GLSurfaceView.requestRender();
            }
        });
        return mSurfaceTexture;
    }

    public void updateSegRes(Bitmap res) {
        if (mSegFilter != null) {
            mSegFilter.updateSegRes(res);
        }
    }
}
