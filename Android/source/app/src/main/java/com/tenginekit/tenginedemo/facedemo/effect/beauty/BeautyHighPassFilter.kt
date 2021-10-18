package com.tenginekit.tenginedemo.facedemo.effect.beauty

import android.opengl.GLES30
import android.opengl.Matrix
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class BeautyHighPassFilter {
    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var inputTexture = -1
    var blurTexture = -1

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
    private var uSTMMatrixLocation = 2

    private var inputTextureLocation = 3
    private var blurTextureLocation = 4

    private var vert = """
#version 300 es
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec4 vTexCoord;
layout(location = 2) uniform mat4 uCameraTextureMatrix;

out vec2 textureCoordinate;

void main(){
    gl_Position = vPosition;  
    textureCoordinate = (uCameraTextureMatrix * vTexCoord).xy; 
}
    """.trimIndent()


    private var frag = """
        #version 300 es
        precision mediump float;
        in vec2 textureCoordinate;
        layout(location = 3) uniform sampler2D inputTexture;
        layout(location = 4) uniform sampler2D blurTexture;
        const float intensity = 24.0;
        out vec4 outColor;
        void main(){
            lowp vec4 sourceColor = texture(inputTexture, textureCoordinate);
            lowp vec4 blurColor = texture(blurTexture, textureCoordinate);
            highp vec4 hightPassColor = sourceColor - blurColor;
            hightPassColor.r = clamp(2.0 * hightPassColor.r * hightPassColor.r * intensity, 0.0, 1.0);
            hightPassColor.g = clamp(2.0 * hightPassColor.g * hightPassColor.g * intensity, 0.0, 1.0);
            hightPassColor.b = clamp(2.0 * hightPassColor.b * hightPassColor.b * intensity, 0.0, 1.0);
            outColor = vec4(hightPassColor.rgb, 1.0);
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
        onDrawFrame(transformMatrix)
    }

    private fun onDrawFrame(transformMatrix: FloatArray) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(shaderProgram)
        Matrix.setIdentityM(transformMatrix, 0)
        GLES30.glUniformMatrix4fv(uSTMMatrixLocation, 1, false, transformMatrix, 0)

        GLES30.glEnableVertexAttribArray(vPositionLocation)
        GLES30.glVertexAttribPointer(vPositionLocation, 3, GLES30.GL_FLOAT, false, 12, buffer)

        GLES30.glEnableVertexAttribArray(vTexCoordLocation)
        GLES30.glVertexAttribPointer(
            vTexCoordLocation,
            2,
            GLES30.GL_FLOAT,
            false,
            8,
            buffer1
        )

        // input Texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(inputTextureLocation, inputTexture)
        GLES30.glUniform1i(inputTextureLocation, 0)


        // blur texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(blurTextureLocation, blurTexture)
        GLES30.glUniform1i(blurTextureLocation, 1)

        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)
    }

}

