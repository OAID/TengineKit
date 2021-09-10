package com.tenginekit.tenginedemo.utils

import android.content.Context
import android.text.TextUtils
import com.tenginekit.tenginedemo.utils.OpenGLUtils
import kotlin.jvm.Synchronized
import android.graphics.Bitmap
import kotlin.jvm.JvmOverloads
import android.graphics.BitmapFactory
import android.opengl.*
import android.util.Log
import java.io.*
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.ArrayList
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL11

object OpenGLUtils {
    const val TAG = "OpenGLUtils"

    // 从初始化失败
    const val GL_NOT_INIT = -1

    // 没有Texture
    const val GL_NOT_TEXTURE = -1

    // 单位矩阵
    val IDENTITY_MATRIX: FloatArray
    private const val SIZEOF_FLOAT = 4
    private const val SIZEOF_SHORT = 2

    /**
     * 从文件路径中读取shader字符串
     * @param filePath
     * @return
     */
    fun getShaderFromFile(filePath: String?): String? {
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val file = File(filePath)
        if (file.isDirectory) {
            return null
        }
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return getShaderStringFromStream(inputStream)
    }

    /**
     * 从Assets文件夹中读取shader字符串
     * @param context
     * @param path      shader相对路径
     * @return
     */
    fun getShaderFromAssets(context: Context, path: String?): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.resources.assets.open(path!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return getShaderStringFromStream(inputStream)
    }

    /**
     * 从输入流中读取shader字符创
     * @param inputStream
     * @return
     */
    private fun getShaderStringFromStream(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
            reader.close()
            return builder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 创建program
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    @Synchronized
    fun createProgram(vertexSource: String?, fragmentSource: String?): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            return 0
        }
        var program = GLES30.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES30.glAttachShader(program, fragmentShader)
        checkGlError("glAttachShader")
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES30.glGetProgramInfoLog(program))
            GLES30.glDeleteProgram(program)
            program = 0
        }
        if (vertexShader > 0) {
            GLES30.glDetachShader(program, vertexShader)
            GLES30.glDeleteShader(vertexShader)
        }
        if (fragmentShader > 0) {
            GLES30.glDetachShader(program, fragmentShader)
            GLES30.glDeleteShader(fragmentShader)
        }
        return program
    }

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

    /**
     * 创建FloatBuffer
     * @param coords
     * @return
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer? {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    /**
     * 创建FloatBuffer
     * @param data
     * @return
     */
    fun createFloatBuffer(data: ArrayList<Float>): FloatBuffer? {
        val coords = FloatArray(data.size)
        for (i in coords.indices) {
            coords[i] = data[i]
        }
        return createFloatBuffer(coords)
    }

    /**
     * 创建ShortBuffer
     * @param coords
     * @return
     */
    fun createShortBuffer(coords: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_SHORT)
        bb.order(ByteOrder.nativeOrder())
        val sb = bb.asShortBuffer()
        sb.put(coords)
        sb.position(0)
        return sb
    }

    /**
     * 创建ShortBuffer
     * @param data
     * @return
     */
    fun createShortBuffer(data: ArrayList<Short>): ShortBuffer {
        val coords = ShortArray(data.size)
        for (i in coords.indices) {
            coords[i] = data[i]
        }
        return createShortBuffer(coords)
    }

    /**
     * 创建Sampler2D的Framebuffer 和 Texture
     * @param frameBuffer
     * @param frameBufferTexture
     * @param width
     * @param height
     */
    fun createFrameBuffer(
        frameBuffer: IntArray, frameBufferTexture: IntArray,
        width: Int, height: Int
    ) {
        GLES30.glGenFramebuffers(frameBuffer.size, frameBuffer, 0)
        GLES30.glGenTextures(frameBufferTexture.size, frameBufferTexture, 0)
        for (i in frameBufferTexture.indices) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTexture[i])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[i])
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, frameBufferTexture[i], 0
            )
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        }
        checkGlError("createFrameBuffer")
    }

    /**
     * 创建Texture对象
     * @param textureType
     * @return
     */
    fun createTexture(textureType: Int): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        checkGlError("glGenTextures")
        val textureId = textures[0]
        GLES30.glBindTexture(textureType, textureId)
        checkGlError("glBindTexture $textureId")
        GLES30.glTexParameterf(
            textureType,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            textureType,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            textureType,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            textureType,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        checkGlError("glTexParameter")
        return textureId
    }


    /**
     * 加载mipmap纹理
     * @param bitmap bitmap图片
     * @return
     */
    fun createGrayTexture(bitmap: Bitmap?): Int {
        val texture = IntArray(1)
        if (bitmap != null && !bitmap.isRecycled) {
            //生成纹理
            GLES30.glGenTextures(1, texture, 0)
            checkGlError("glGenTexture")
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            return texture[0]
        }
        return 0
    }


    fun loadTexture(data: ByteBuffer?, width: Int, height: Int, type: Int, usedTexId: Int): Int {
        var usedTexId = usedTexId
        if (usedTexId == -1) {
            val textures = IntArray(1)
            GLES30.glGenTextures(1, textures, 0)
            usedTexId = textures[0]
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, type, width, height,
                0, type, GLES30.GL_UNSIGNED_BYTE, null
            )
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId)
        GLES30.glTexSubImage2D(
            GLES30.GL_TEXTURE_2D, 0, 0, 0, width,
            height, type, GLES30.GL_UNSIGNED_BYTE, data
        )
        return usedTexId
    }


    /**
     * 加载mipmap纹理
     * @param bitmap bitmap图片
     * @return
     */
    fun createTexture(bitmap: Bitmap?): Int {
        val texture = IntArray(1)
        if (bitmap != null && !bitmap.isRecycled) {
            //生成纹理
            GLES30.glGenTextures(1, texture, 0)
            checkGlError("glGenTexture")
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            return texture[0]
        }
        return 0
    }

    /**
     * 使用旧的Texture 创建新的Texture (宽高不能大于旧Texture的宽高，主要用于贴纸不断切换图片)
     * @param bitmap
     * @param texture
     * @return
     */
    fun createTexture(bitmap: Bitmap?, texture: Int): Int {
        val result = IntArray(1)
        if (texture == GL_NOT_TEXTURE) {
            result[0] = createTexture(bitmap)
        } else {
            result[0] = texture
            if (bitmap != null && !bitmap.isRecycled) {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, result[0])
                GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, bitmap)
            }
        }
        return result[0]
    }
    /**
     * 创建纹理
     * @param bytes
     * @param width
     * @param height
     * @param texture
     * @return
     */
    /**
     * 创建Texture
     * @param bytes
     * @param width
     * @param height
     * @return
     */
    @JvmOverloads
    fun createTexture(
        bytes: ByteArray,
        width: Int,
        height: Int,
        texture: Int = GL_NOT_TEXTURE
    ): Int {
        if (bytes.size != width * height * 4) {
            throw RuntimeException("Illegal byte array")
        }
        return createTexture(ByteBuffer.wrap(bytes), width, height, texture)
    }

    /**
     * 创建Texture
     * @param byteBuffer
     * @param width
     * @param height
     * @return
     */
    fun createTexture(byteBuffer: ByteBuffer, width: Int, height: Int): Int {
        if (byteBuffer.array().size != width * height * 4) {
            throw RuntimeException("Illegal byte array")
        }
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        if (texture[0] == 0) {
            Log.d(TAG, "Failed at glGenTextures")
            return 0
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
            width, height, 0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            byteBuffer
        )
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return texture[0]
    }

    /**
     * 使用旧的Texture 创建新的Texture (宽高不能大于旧Texture的宽高，主要用于贴纸不断切换图片)
     * @param byteBuffer
     * @param width
     * @param height
     * @param texture
     * @return
     */
    fun createTexture(byteBuffer: ByteBuffer, width: Int, height: Int, texture: Int): Int {
        if (byteBuffer.array().size != width * height * 4) {
            throw RuntimeException("Illegal byte array")
        }
        val result = IntArray(1)
        if (texture == GL_NOT_TEXTURE) {
            return createTexture(byteBuffer, width, height)
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
            GLES30.glTexSubImage2D(
                GLES30.GL_TEXTURE_2D, 0, 0, 0,
                width, height,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                byteBuffer
            )
            result[0] = texture
        }
        return result[0]
    }

    /**
     * 使用绝对路径创建纹理
     * @param filePath  mipmap图片路径
     * @return  纹理Id，失败则返回GL_NO_TEXTURE;
     */
    fun createTexture(filePath: String): Int {
        val textureHandle = IntArray(1)
        textureHandle[0] = GL_NOT_TEXTURE
        if (TextUtils.isEmpty(filePath)) {
            return GL_NOT_TEXTURE
        }
        GLES30.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0])
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        Log.d(
            "createTextureFromAssets", "filePath:" + filePath
                    + ", texture = " + textureHandle[0]
        )
        return textureHandle[0]
    }

    /**
     * 加载mipmap纹理
     * @param context
     * @param name
     * @return
     */
    fun createTextureFromAssets(context: Context, name: String?): Int {
        val textureHandle = IntArray(1)
        GLES30.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val bitmap: Bitmap? = BitmapUtils.getImageFromAssetsFile(context, name)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0])
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap?.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        return textureHandle[0]
    }

    /**
     * 创建OES 类型的Texture
     * @return
     */
    fun createOESTexture(): Int {
        return createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
    }

    /**
     * 删除纹理
     * @param texture
     */
    fun deleteTexture(texture: Int) {
        val textures = IntArray(1)
        textures[0] = texture
        GLES30.glDeleteTextures(1, textures, 0)
    }
    /**
     * 绑定纹理
     * @param location  句柄
     * @param texture   纹理值
     * @param index     绑定的位置
     * @param textureType 纹理类型
     */
    /**
     * 绑定纹理
     * @param location  句柄
     * @param texture   纹理id
     * @param index     索引
     */
    @JvmOverloads
    fun bindTexture(
        location: Int,
        texture: Int,
        index: Int,
        textureType: Int = GLES30.GL_TEXTURE_2D
    ) {
        // 最多支持绑定32个纹理
        require(index <= 31) { "index must be no more than 31!" }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + index)
        GLES30.glBindTexture(textureType, texture)
        GLES30.glUniform1i(location, index)
    }

    /**
     * 获取出错信息
     * @param error
     * @return
     */
    fun getErrorString(error: Int): String {
        return when (error) {
            EGL10.EGL_SUCCESS -> "EGL_SUCCESS"
            EGL10.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
            EGL10.EGL_BAD_ACCESS -> "EGL_BAD_ACCESS"
            EGL10.EGL_BAD_ALLOC -> "EGL_BAD_ALLOC"
            EGL10.EGL_BAD_ATTRIBUTE -> "EGL_BAD_ATTRIBUTE"
            EGL10.EGL_BAD_CONFIG -> "EGL_BAD_CONFIG"
            EGL10.EGL_BAD_CONTEXT -> "EGL_BAD_CONTEXT"
            EGL10.EGL_BAD_CURRENT_SURFACE -> "EGL_BAD_CURRENT_SURFACE"
            EGL10.EGL_BAD_DISPLAY -> "EGL_BAD_DISPLAY"
            EGL10.EGL_BAD_MATCH -> "EGL_BAD_MATCH"
            EGL10.EGL_BAD_NATIVE_PIXMAP -> "EGL_BAD_NATIVE_PIXMAP"
            EGL10.EGL_BAD_NATIVE_WINDOW -> "EGL_BAD_NATIVE_WINDOW"
            EGL10.EGL_BAD_PARAMETER -> "EGL_BAD_PARAMETER"
            EGL10.EGL_BAD_SURFACE -> "EGL_BAD_SURFACE"
            EGL11.EGL_CONTEXT_LOST -> "EGL_CONTEXT_LOST"
            else -> getHex(error)
        }
    }

    private fun getHex(value: Int): String {
        return "0x" + Integer.toHexString(value)
    }

    init {
        IDENTITY_MATRIX = FloatArray(16)
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }
}
