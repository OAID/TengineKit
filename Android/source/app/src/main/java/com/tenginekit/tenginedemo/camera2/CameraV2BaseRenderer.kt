package com.tenginekit.tenginedemo.camera2

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.tengine.cameratest.CameraFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class CameraV2BaseRenderer(
) : GLSurfaceView.Renderer {
    var cameraV2GLSurfaceView: CameraV2GLSurfaceView? = null
    var cameraManager: CameraV2Manager? = null
    var mSurfaceTexture: SurfaceTexture? = null
    val transformMatrix = FloatArray(16)
    var cameraFilter: CameraFilter? = null
    var context: Context? = null

    fun setUp(surfaceView: CameraV2GLSurfaceView, camera: CameraV2Manager, activity: Activity) {
        cameraV2GLSurfaceView = surfaceView
        cameraManager = camera
        context = activity
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        // super part
        cameraFilter = CameraFilter()
        cameraManager!!.setSurfaceCreated(true)
        cameraManager!!.openCamera()
        // super part
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // super part
        GLES20.glViewport(0, 0, width, height)
        cameraFilter!!.onSurfaceChanged(width, height)
        Log.i(TAG, "onSurfaceChanged: $width, $height")
        // super part
    }

    override fun onDrawFrame(gl: GL10) {
        //super part
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.updateTexImage()
            mSurfaceTexture!!.getTransformMatrix(transformMatrix)
        }
        //super part
    }

    fun getSurfaceTexture(): SurfaceTexture {
        mSurfaceTexture = SurfaceTexture(cameraFilter!!.oESTextureId)
        mSurfaceTexture!!.setOnFrameAvailableListener { cameraV2GLSurfaceView!!.requestRender() }
        return mSurfaceTexture!!
    }

    companion object {
        const val TAG = "CameraV2BaseRenderer"
    }
}
