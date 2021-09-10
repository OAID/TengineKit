package com.tenginekit.tenginedemo.camera2

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class SegFilter {

    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    private val buffer2: FloatBuffer?

    var shaderProgram = -1
    private var width = 0
    private var height = 0

    var upstreamTexture = -1
    var segBitmap: Bitmap? = null
    var segTexture = -1


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

    private val textureSegData = floatArrayOf(
        1f, 0f,
        0f, 0f,
        1f, 1f,
        0f, 1f
    )

    private val projectionMatrix = FloatArray(16)
    private val grayMatrix = FloatArray(16)

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }


    private fun onDrawFrame(transformMatrix: FloatArray) {
        Matrix.orthoM(
            projectionMatrix, 0,
            -1f, 1f, -1f, 1f,
            -1f, 1f
        )
        Matrix.setIdentityM(grayMatrix, 0);
        Matrix.translateM(grayMatrix, 0, 0.5f, 0.5f, 0f)
        Matrix.rotateM(grayMatrix, 0, 180f, 0f, 0f, 1f)
        Matrix.translateM(grayMatrix, 0, -0.5f, -0.5f, 0f)

        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)




        GLES30.glUseProgram(shaderProgram)
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)
        GLES30.glUniformMatrix4fv(uSTMMatrixLocation, 1, false, transformMatrix, 0)
        GLES30.glUniformMatrix4fv(uGrayMatrix, 1, false, grayMatrix, 0)

        GLES30.glEnableVertexAttribArray(aPositionLocation)
        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 12, buffer)


        GLES30.glEnableVertexAttribArray(vGrayCoo);
        GLES30.glVertexAttribPointer(vGrayCoo, 2, GLES30.GL_FLOAT, false, 8, buffer2);

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
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, upstreamTexture)
        GLES30.glUniform1i(uSTextureLocation, 0)

        segBitmap?.let {
            segTexture = OpenGLUtils.createTexture(segBitmap, segTexture)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, segTexture)
            GLES30.glUniform1i(uGrayTextureLocation, 1)
        }


        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun onDrawFrameScreen(transformMatrix: FloatArray) {
        onDrawFrame(transformMatrix)
    }

    fun updateSegRes(res: Bitmap) {
        segBitmap = res;
    }


    private val aPositionLocation = 0
    private val aTextureCoordLocation = 1
    private val uMatrixLocation = 2
    private val uSTMMatrixLocation = 3
    private val vGrayCoo = 4;
    private val uGrayMatrix = 5;
    private val uSTextureLocation = 6
    private val uGrayTextureLocation = 7


    private val vrg = """
       #version 300 es
layout (location = 0) in vec4 vPosition;//顶点位置
layout (location = 1) in vec4 vTexCoord;//纹理坐标
layout (location = 2) uniform mat4 uMatrix; //顶点变换矩阵
layout (location = 3) uniform mat4 uCameraTextureMatrix; //纹理变换矩阵
layout (location = 4) in vec4 vGrayCoo; // mask变换
layout (location = 5) uniform mat4 uGrayTextureMatrix; //

out vec2 texCoo2Frag; 
out vec2 grayTexCoo;

void main() {
    texCoo2Frag = (uCameraTextureMatrix * vTexCoord).xy;
    grayTexCoo =  (uGrayTextureMatrix * vGrayCoo).xy;
    gl_Position = vPosition;
}
    """.trimIndent()

    private var frag = """
        #version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 texCoo2Frag;
in vec2 grayTexCoo;
out vec4 outColor;

layout(location = 6)uniform samplerExternalOES sTexture;
layout(location = 7)uniform sampler2D grayTexture;

void main() {
    float gray = texture(grayTexture, grayTexCoo).r;
    vec4 rgba = texture(sTexture, texCoo2Frag);
    if (gray > 0.01) {
        outColor = rgba;
    } else {
        outColor = vec4(0.0784, 0.7059, 0.1294, 1.0);   
    }   
}
    """.trimIndent()

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        buffer2 = OpenGLUtils.createFloatBuffer(textureSegData)
        shaderProgram = OpenGLUtils.createProgram(vrg, frag)
    }

}
