package com.tenginekit.tenginedemo.facedemo.effect.beauty

import android.opengl.GLES30
import android.opengl.Matrix
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class BeautyBlurFilter(size: Int) {
    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var upstreamTexture = -1
    var widthOffset: Float = 0f
    var heightOffset: Float = 0f

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


    fun setTexelOffset(widthOffset: Float, heightOffset: Float) {
        this.widthOffset = widthOffset / this.width
        this.heightOffset = heightOffset / this.height
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

    private var widthOffsetLocation = 3
    private var heightOffsetLocation = 4
    private var inputTextureLocation = 5

    private var vert = """
        #version 300 es
        #define SHIFT_SIZE 5
        layout(location = 0) in vec4 vPosition;
        layout(location = 1) in vec4 vTexCoord;
        layout(location = 2) uniform mat4 uCameraTextureMatrix;
       

        layout(location = 3) uniform highp float texelWidthOffset;
        layout(location = 4) uniform highp float texelHeightOffset;

        out vec2 textureCoordinate;
        out vec4 blurShiftCoordinates[SHIFT_SIZE];

        void main(){
            gl_Position = vPosition;
            textureCoordinate = (uCameraTextureMatrix * vTexCoord).xy;
            vec2 step = vec2(texelWidthOffset, texelHeightOffset);
            for(int i = 0; i < SHIFT_SIZE; i++){
                blurShiftCoordinates[i] = vec4(
                    textureCoordinate - float(i + 1) * step,
                    textureCoordinate + float(i + 1) * step
                );
            }  
        }
    """.trimIndent()


    private var frag = """
        #version 300 es
        #define SHIFT_SIZE 5
precision highp float;
in vec2 textureCoordinate;
out vec4 outColor;

layout(location = 5) uniform sampler2D inputTexture;
in vec4 blurShiftCoordinates[SHIFT_SIZE];

void main() {
    vec4 currentColor = texture(inputTexture, textureCoordinate);
    mediump vec3 sum = currentColor.rgb;
    for(int i = 0; i < SHIFT_SIZE; i ++ ) {
        sum += texture(inputTexture, blurShiftCoordinates[i].xy).rgb;
        sum += texture(inputTexture, blurShiftCoordinates[i].zw).rgb;
    }
    
    outColor = vec4((sum * 1.0) / float(2 * SHIFT_SIZE + 1), currentColor.a);
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
        // init offset ..
        GLES30.glUniform1f(widthOffsetLocation, widthOffset)
        GLES30.glUniform1f(heightOffsetLocation, heightOffset)

        // bind texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, upstreamTexture)
        GLES30.glUniform1i(inputTextureLocation, 0)

        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    init{
        vert = vert.replace("#define SHIFT_SIZE 5","#define SHIFT_SIZE $size")
        frag = frag.replace("#define SHIFT_SIZE 5","#define SHIFT_SIZE $size")

        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)
    }

}
