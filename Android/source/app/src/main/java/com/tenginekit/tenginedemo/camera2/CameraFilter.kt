package com.tengine.cameratest

import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer
import android.opengl.GLES30.GL_FRAMEBUFFER

import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLES30.GL_COLOR_ATTACHMENT0
import android.opengl.GLES30.glFramebufferTexture2D


class CameraFilter {
    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var oESTextureId = -1
    var shaderProgram = -1
    private var width = 0
    private var height = 0

    private var TAG = Constant.LOG_TAG;


    var frameBuffer = IntArray(1)
    var frameTexture = IntArray(1)


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


    private val vrg = """
       #version 300 es
layout (location = 0) in vec4 vPosition;//顶点位置
layout (location = 1) in vec4 vTexCoord;//纹理坐标
layout (location = 2) uniform mat4 uMatrix; //顶点变换矩阵
layout (location = 3) uniform mat4 uSTMatrix; //纹理变换矩阵

out vec2 texCoo2Frag; 

void main() {
    texCoo2Frag = (uSTMatrix * vTexCoord).xy;
    gl_Position = uMatrix*vPosition;
}
    """.trimIndent()

    private var frag = """
        #version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

in vec2 texCoo2Frag;
out vec4 outColor;

layout (location = 4) uniform samplerExternalOES sTexture;

void main() {
    outColor = texture(sTexture, texCoo2Frag);
}
    """.trimIndent()

    private val projectionMatrix = FloatArray(16)


    fun onDrawFrameBuffer(transformMatrix: FloatArray): Int {
        //bindFrameBufferAndTexture()
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0])
        onDrawFrame(transformMatrix)
        //unBindFrameBuffer()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        return frameTexture[0]
    }

    private fun onDrawFrame(transformMatrix: FloatArray) {
        Matrix.orthoM(
            projectionMatrix, 0,
            -1f, 1f, -1f, 1f,
            -1f, 1f
        )

        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(shaderProgram)
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)
        GLES30.glUniformMatrix4fv(uSTMMatrixLocation, 1, false, transformMatrix, 0)

        GLES30.glEnableVertexAttribArray(aPositionLocation)
        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 12, buffer)

        GLES30.glEnableVertexAttribArray(aTextureCoordLocation)
        GLES30.glVertexAttribPointer(
            aTextureCoordLocation,
            2,
            GLES30.GL_FLOAT,
            false,
            8,
            buffer1
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        // bindTexture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oESTextureId)
        GLES30.glUniform1i(uSTextureLocation, 0)

        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun onDrawFrameScreen(transformMatrix: FloatArray) {
        onDrawFrame(transformMatrix)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        delFrameBufferAndTexture()
        genFrameBufferAndTexture()
    }


    fun delFrameBufferAndTexture() {
        GLES30.glDeleteFramebuffers(frameBuffer.size, frameBuffer, 0)
        GLES30.glDeleteTextures(frameTexture.size, frameTexture, 0)
    }

    private fun genFrameBufferAndTexture() {
        OpenGLUtils.createFrameBuffer(frameBuffer, frameTexture, width, height)
    }

    private fun bindFrameBufferAndTexture() {
        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer[0])
//        GLES30.glFramebufferTexture2D(
//            GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
//            frameTexture[0], 0
//        )
    }

    fun unBindFrameBuffer() {
        GLES30.glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private val aPositionLocation = 0
    private val aTextureCoordLocation = 1
    private val uMatrixLocation = 2
    private val uSTMMatrixLocation = 3
    private val uSTextureLocation = 4


    init {

        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        oESTextureId = textures[0]


        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oESTextureId)



        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vrg, frag)
    }
}
