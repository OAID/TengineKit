package com.tenginekit.tenginedemo.facedemo.effect.reshpae

import android.graphics.PointF
import android.opengl.GLES30
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import java.nio.FloatBuffer

class FaceReshapeFilter {

    companion object{
        const val leftOriginIndex = 65
        const val leftTargetIndex = 144
        const val leftEdgeIndex = 60

        const val rightOriginIndex = 48
        const val rightTargetIndex = 160
    }


    private val buffer: FloatBuffer?
    private val buffer1: FloatBuffer?
    var shaderProgram = -1

    private var width = 0
    private var height = 0
    var upstreamTexture = -1

    var frameBuffer = IntArray(1)
    var frameTexture = IntArray(1)

    // send to fragment  lefteyecenter righteyecenter
    var bufferPoints: FloatBuffer? = null
    var points = FloatArray(7 * 2)

    var bufferStretch: FloatBuffer? = null
    var stretch = FloatArray(4)

    val eyeIndex = intArrayOf(
        102 * 2, 102 * 2 + 1, 110 * 2, 110 * 2 + 1,
        126 * 2, 126 * 2 + 1, 118 * 2, 118 * 2 + 1
    )
    var landmarkFloatArray: FloatArray? = null


    fun onLandmark(floatArray: FloatArray?) {
        landmarkFloatArray = floatArray
    }

    private fun distance(pointA: PointF, pointB: PointF): Float {
        return (pointA.x - pointB.x) * (pointA.x - pointB.x) + (pointA.y - pointB.y) * (pointA.y - pointB.y);
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
    private var inputTextureLocation = 2
    private var pointsLocation = 4
    private var ratioLocation = 3
    private var strechValueLocation = 12

    private var vert = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;
        layout(location = 1) in vec4 vTexCoord;

        out vec2 textureCoordinate;

        void main(){
            gl_Position = vPosition;  
            textureCoordinate = vTexCoord.xy; 
        }
    """.trimIndent()


    private var frag = """
        #version 300 es
        #define POINTS 7
        #define STRECH_LEN 4
        layout(location = 2)uniform sampler2D inputTexture;
        layout(location = 3)uniform float ratio;
        layout(location = 4)uniform vec2 points[POINTS];
        // lefteye center / righteye center / leftoriginPosition / leftTargetPostion / leftEdgePostion /
        // rightoriginPosition / rightTargetPosition / rightEdgePosition
        layout(location = 12)uniform float strechValue[STRECH_LEN];
        // left radius / left distOri / right radius / right distOri


        in vec2 textureCoordinate;
        out vec4 outColor;

        // 鼠标在originPosition位置 然后鼠标originPosition -> target方向拉伸 形成瘦脸效果
        vec2 stretchFun(vec2 currentCoord, vec2 originPosition, vec2 targetPosition, float radius, float distOri, float ratio) {
            float r = distance(currentCoord, originPosition);
            vec2 newCoord = currentCoord;
            float tmp = r * r;
            if (tmp >= radius) {
                return newCoord;
            }
            float weight = (radius - tmp) / (radius - tmp + distOri);
            newCoord = currentCoord - weight * weight * (targetPosition - originPosition);
            return newCoord;
        }

        vec2 localScale(vec2 currentCoord, vec2 center, float ratio, float rmax) {
            float r = distance(currentCoord, center);
            vec2 afterCoord = currentCoord;
            if (r < rmax) {
                float weight = (1.0 - ratio * pow((r / rmax - 1.0), 2.0));
                afterCoord = weight * (currentCoord - center) + center;
            }
            return afterCoord;
        }

        void main() {
            float rmax = distance(points[0], points[1]) / 4.0;
            vec2 coord = localScale(textureCoordinate, points[0], ratio, rmax);
            coord = localScale(coord, points[1], ratio, rmax);
            
            coord = stretchFun(coord, points[2], points[3], strechValue[0], strechValue[1], 1.0);
            coord = stretchFun(coord, points[5], points[6], strechValue[2], strechValue[3], 1.0);
            
            outColor = texture(inputTexture, coord);
            
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
        onData()
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(shaderProgram)
        // onData


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
        //
        GLES30.glUniform2fv(pointsLocation, 7, bufferPoints)
        GLES30.glUniform1f(ratioLocation, 0.1f)
        GLES30.glUniform1fv(strechValueLocation, 4, bufferStretch)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, upstreamTexture)
        GLES30.glUniform1i(inputTextureLocation, 0)


        // start draw
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun onData() {
        landmarkFloatArray?.let { floatArray ->

            // leftEyeCenter
            var x0 = floatArray[eyeIndex[0]]
            var x1 = floatArray[eyeIndex[2]]
            points[0] = (x0 + x1) / 2
            var y0 = 1 - floatArray[eyeIndex[1]]
            var y1 = 1 - floatArray[eyeIndex[3]]
            points[1] = (y0 + y1) / 2

            // rightEyeCenter
            x0 = floatArray[eyeIndex[4]]
            x1 = floatArray[eyeIndex[6]]
            points[2] = (x0 + x1) / 2
            y0 = 1 - floatArray[eyeIndex[5]]
            y1 = 1 - floatArray[eyeIndex[7]]
            points[3] = (y0 + y1) / 2


            //
            val leftOriginCoord = PointF(floatArray[leftOriginIndex * 2], 1 - floatArray[leftOriginIndex * 2 + 1])
            val leftEdgeCoord = PointF(floatArray[leftEdgeIndex * 2], 1 - floatArray[leftEdgeIndex * 2 + 1])
            var leftRadius = distance(leftOriginCoord, leftEdgeCoord)

            val leftTargetCoord = PointF(floatArray[leftTargetIndex * 2], 1 - floatArray[leftTargetIndex * 2 + 1])
            val leftDist = distance(leftTargetCoord, leftOriginCoord)
            leftRadius = leftDist * 0.2.toFloat()

            val rightOriginCoord = PointF(floatArray[rightOriginIndex * 2], 1 - floatArray[rightOriginIndex * 2 + 1])
            //val rightEdgeCoord = PointF(floatArray[rightEdgeIndex * 2], 1 - floatArray[leftEdgeIndex * 2 + 1])
            //var leftRadius = distance(leftOriginCoord, leftEdgeCoord)

            val rightTargetCoord = PointF(floatArray[rightTargetIndex * 2], 1 - floatArray[rightTargetIndex * 2 + 1])
            val rightDist = distance(rightTargetCoord, rightOriginCoord)
            val rightRadius = rightDist * 0.2.toFloat()



            points[4] = leftOriginCoord.x
            points[5] = leftOriginCoord.y
            points[6] = leftTargetCoord.x
            points[7] = leftTargetCoord.y
            points[8] = leftEdgeCoord.x
            points[9] = leftEdgeCoord.y


            points[10] = rightOriginCoord.x
            points[11] = rightOriginCoord.y
            points[12] = rightTargetCoord.x
            points[13] = rightTargetCoord.y

            stretch[0] = leftRadius
            stretch[1] = leftDist

            stretch[2] = rightRadius
            stretch[3] = rightDist


            if (bufferPoints == null) {
                bufferPoints = OpenGLUtils.createFloatBuffer(points)
            } else {
                bufferPoints?.position(0)
                bufferPoints?.clear()
                bufferPoints?.put(points)
                bufferPoints?.position(0)
            }

            if (bufferStretch == null) {
                bufferStretch = OpenGLUtils.createFloatBuffer(stretch)
            } else {
                bufferStretch?.position(0)
                bufferStretch?.clear()
                bufferStretch?.put(stretch)
                bufferStretch?.position(0)
            }


        }
    }

    init {
        buffer = OpenGLUtils.createFloatBuffer(vertexData)
        buffer1 = OpenGLUtils.createFloatBuffer(textureVertexData)
        shaderProgram = OpenGLUtils.createProgram(vert, frag)
    }

}
