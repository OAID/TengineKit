package com.tenginekit.tenginedemo.segdemo

import android.graphics.Bitmap
import com.tenginekit.tenginedemo.camera2.CameraV2BaseRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleSegRenderer: CameraV2BaseRenderer() {

    private var segFilter: SegFilter? = null
    override fun onDrawFrame(gl: GL10) {
        super.onDrawFrame(gl)

        val outTexture = cameraFilter!!.onDrawFrameBuffer(transformMatrix)
        segFilter!!.upstreamTexture = outTexture
        segFilter!!.onDrawFrameScreen(transformMatrix)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        segFilter?.onSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        super.onSurfaceCreated(gl, config)
        segFilter = SegFilter()
    }


    fun updateSegRes(res: Bitmap) {
        res?.let {
            segFilter?.updateSegRes(it)
        }
    }
}
