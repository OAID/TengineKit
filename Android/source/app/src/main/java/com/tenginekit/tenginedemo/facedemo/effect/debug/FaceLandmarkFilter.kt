package com.tenginekit.tenginedemo.facedemo.effect.debug

import android.opengl.GLES30
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class FaceLandmarkFilter {
    companion object {
        const val LANDMARK_NUM = 212
    }

    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var upstreamTexture = -1

    var frameBuffer = IntArray(1)
    var frameTexture = IntArray(1)

    var landmark = FloatArray(LANDMARK_NUM * 3)
    var landmarkBuffer: FloatBuffer? = null

    fun onLandmark(floatArray: FloatArray?) {
        floatArray?.let {
            for (i in 0 until LANDMARK_NUM) {
                landmark[i * 3] = (it[i * 2] * 2) - 1
                landmark[i * 3 + 1] = 1 - (it[i * 2 + 1] * 2)
                landmark[i * 3 + 2] = 0.0f
            }
            if (landmarkBuffer == null) {
                landmarkBuffer = OpenGLUtils.createFloatBuffer(landmark)
            } else {
                landmarkBuffer?.position(0)
                landmarkBuffer?.clear()
                landmarkBuffer?.put(landmark)
                landmarkBuffer?.position(0)
            }
        }
    }


    private val vertexData = floatArrayOf(
        1f, -1f, 0f,
        -1f, -1f, 0f,
        1f, 1f, 0f,
        -1f, 1f, 0f
    )

    private val textureVertexData = floatArrayOf(
        1f, 0f,
        0f, 0f,
        1f, 1f,
        0f, 1f
    )

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        delFrameBufferAndTexture()
        genFrameBufferAndTexture()
    }


    private fun delFrameBufferAndTexture() {
        GLES30.glDeleteFramebuffers(frameBuffer.size, frameBuffer, 0)
        GLES30.glDeleteTextures(frameTexture.size, frameTexture, 0)
    }

    private fun genFrameBufferAndTexture() {
        OpenGLUtils.createFrameBuffer(frameBuffer, frameTexture, width, height)
    }


    private var vPositionLocation = 0
    private var vTexCoordLocation = 1


    private var vert = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;

        void main(){
            gl_Position = vPosition;
            gl_PointSize = 8.0;   
        }
    """.trimIndent()


    private var frag = """
        #version 300 es
        precision mediump float;
        out vec4 outColor;
        void main(){
           outColor = vec4(0.8118, 0.102, 0.102, 1.0);
        }
    """.trimIndent()


    fun onDrawFrameBuffer(transformMatrix: FloatArray): Int {
        //bindFrameBufferAndTexture()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0])
        onDrawFrame(transformMatrix)
        //unBindFrameBuffer()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        return frameTexture[0]
    }

    fun onDrawFrameScreen(transformMatrix: FloatArray) {
        landmarkBuffer?.let {
            onDrawFrame(transformMatrix)
        }
    }

    private fun onDrawFrame(transformMatrix: FloatArray) {
        // reset and init
        //GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(shaderProgram)
        // common matrix and coord


        GLES30.glEnableVertexAttribArray(vPositionLocation)
        GLES30.glVertexAttribPointer(vPositionLocation, 3, GLES30.GL_FLOAT, false, 12, landmarkBuffer)


        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, LANDMARK_NUM)
        // start draw
    }

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)
    }

}
