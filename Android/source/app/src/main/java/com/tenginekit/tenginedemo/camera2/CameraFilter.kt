package com.tengine.cameratest

import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.tengine.cameratest.CameraV2Manager.TAG
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraFilter {
    val buffer: FloatBuffer?
    val buffer1: FloatBuffer?
    var oESTextureId = -1
    var shaderProgram = -1

    /**
     * 创建FloatBuffer
     * @param coords
     * @return
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer? {
        val bb = ByteBuffer.allocateDirect(coords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    fun linkProgram(verShader: Int, fragShader: Int): Int {
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, verShader)
        GLES30.glAttachShader(program, fragShader)
        GLES30.glLinkProgram(program)
        return program
    }

    companion object {


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
    }


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

    /**
     * 加载Shader
     * @param shaderType
     * @param source
     * @return
     */
    fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES30.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }


    /**
     * 检查是否出错
     * @param op
     */
    fun checkGlError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            //            throw new RuntimeException(msg);
        }
    }

    private val projectionMatrix = FloatArray(16)

    fun onDrawFrame(transformMatrix: FloatArray) {
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

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oESTextureId)
        GLES30.glUniform1i(uSTextureLocation, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    private val aPositionLocation = 0
    private val aTextureCoordLocation = 1
    private val uMatrixLocation = 2
    private val uSTMMatrixLocation = 3
    private val uSTextureLocation = 4


    init {
        //oESTextureId = OESTextureId


        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        oESTextureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oESTextureId)



        buffer = createFloatBuffer(vertexData)
        buffer1 = createFloatBuffer(textureVertexData)
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vrg)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, frag)
        shaderProgram = linkProgram(vertexShader, fragmentShader)
    }
}
