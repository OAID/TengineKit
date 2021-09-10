package com.tenginekit.tenginedemo.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoRender(context: Context, video: File) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener,
    MediaPlayer.OnVideoSizeChangedListener {
    private lateinit var file: File
    private lateinit var context: Context


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(p0: GL10?) {
        TODO("Not yet implemented")
    }

    override fun onFrameAvailable(p0: SurfaceTexture?) {
        TODO("Not yet implemented")
    }

    override fun onVideoSizeChanged(p0: MediaPlayer?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

}
