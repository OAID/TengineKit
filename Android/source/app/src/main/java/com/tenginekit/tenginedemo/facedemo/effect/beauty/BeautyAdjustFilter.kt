package com.tenginekit.tenginedemo.facedemo.effect.beauty

import android.opengl.GLES30
import android.opengl.Matrix
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class BeautyAdjustFilter {
    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var upstreamTexture = -1
    var blurTexture = -1
    var highPassBlurTexture = -1

    var frameBuffer = IntArray(1)
    var frameTexture = IntArray(1)

    var intensity = 0.5f

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
    private var highPassTextureLocation = 5
    private var intensityLocation = 6

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
layout(location = 3)uniform sampler2D inputTexture;
layout(location = 4)uniform sampler2D blurTexture;
layout(location = 5)uniform sampler2D highPassBlurTexture;
layout(location = 6)uniform lowp float intensity;

in vec2 textureCoordinate;
out vec4 outColor;

void main() {
    vec4 sourceColor = texture(inputTexture, textureCoordinate);
    lowp vec4 blurColor = texture(blurTexture, textureCoordinate);
    lowp vec4 highPassBlurColor = texture(highPassBlurTexture, textureCoordinate);
    
    mediump float value = clamp(((min(sourceColor.b, blurColor.b) - 0.2)) * 5.0, 0.0, 1.0);
    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
    mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;
    lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, 0.5);
    outColor = sourceColor;
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
        // reset and init
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(shaderProgram)
        // common matrix and coord
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

        // intensity set
        GLES30.glUniform1f(intensityLocation, intensity)

        // source Texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, upstreamTexture)
        GLES30.glUniform1i(inputTextureLocation, 0)

        // blur Texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, blurTexture)
        GLES30.glUniform1i(blurTextureLocation, 1)

        // highPass blur Texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, highPassBlurTexture)
        GLES30.glUniform1i(highPassTextureLocation, 2)

        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)
    }

}
