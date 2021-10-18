package com.tenginekit.tenginedemo.facedemo.effect.beauty

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class BeautyComplexionFilter(context: Context) {
    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var upstreamTexture = -1
    var grayTexture = -1
    var lookupTexture = -1

    var levelRangeInv = 1.040816f
    var levelBlack = 0.01960784f
    var alpha = 0.7f

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

    //
    private var uSTMMatrixLocation = 2
    private var inputTextureLocation = 3
    private var grayTextureLocation = 4
    private var lookupTextureLocation = 5

    private var levelRangeInvLocation = 6
    private var levelBlackLocation = 7
    private var alphaLocation = 8

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
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;
in vec2 textureCoordinate;
out vec4 outColor;

layout(location = 3) uniform samplerExternalOES inputTexture;
layout(location = 4) uniform sampler2D grayTexture;
layout(location = 5) uniform sampler2D lookupTexture;


layout(location = 6) uniform float levelRangeInv;
layout(location = 7) uniform float levelBlack;
layout(location = 8) uniform float alpha;

void main() {
    lowp vec3 textureColor = texture(inputTexture, textureCoordinate).rgb;
    
    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0, 1.0);
    textureColor.r = texture(grayTexture, vec2(textureColor.r, 0.5)).r;
    textureColor.g = texture(grayTexture, vec2(textureColor.g, 0.5)).g;
    textureColor.b = texture(grayTexture, vec2(textureColor.b, 0.5)).b;
    
    mediump float blueColor = textureColor.b * 15.0;
    
    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);
    
    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);
    
    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);
    
    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);
    
    lowp vec4 newColor1 = texture(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture(lookupTexture, texPos2);
    
    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));
    
    textureColor = mix(textureColor, newColor, alpha);
    
    outColor = vec4(textureColor, 1.0);
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
        GLES30.glUniform1f(levelRangeInvLocation, levelRangeInv)
        GLES30.glUniform1f(levelBlackLocation, levelBlack)
        GLES30.glUniform1f(alphaLocation, alpha)
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
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        // bindTexture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, upstreamTexture)
        GLES30.glUniform1i(inputTextureLocation, 0)

        // grayTexture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, grayTexture)
        GLES30.glUniform1i(grayTextureLocation, 1)

        // lookupTexture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lookupTexture)
        GLES30.glUniform1i(lookupTextureLocation, 2)


        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)

        grayTexture = OpenGLUtils.createTextureFromAssets(context, "texture/skin_gray.png")
        lookupTexture = OpenGLUtils.createTextureFromAssets(context, "texture/skin_lookup.png")
    }

}
