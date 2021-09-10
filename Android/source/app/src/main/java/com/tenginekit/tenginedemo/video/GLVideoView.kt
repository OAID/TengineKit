package com.tenginekit.tenginedemo.video

import android.content.Context
import android.opengl.GLSurfaceView
import java.io.File

class GLVideoView(context: Context?): GLSurfaceView(context) {


    init {
        val mp4Path = "";
        val video = File(mp4Path)
        setEGLContextClientVersion(3)
    }
}
