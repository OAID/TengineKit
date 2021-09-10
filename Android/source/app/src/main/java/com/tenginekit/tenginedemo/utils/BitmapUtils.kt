//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.tenginekit.tenginedemo.utils

import android.content.Context
import android.text.TextUtils
import kotlin.jvm.JvmOverloads
import android.graphics.Bitmap.CompressFormat
import android.content.res.AssetManager
import android.graphics.*
import android.media.ExifInterface
import android.util.Log
import androidx.annotation.IntRange
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception

object BitmapUtils {
    private const val WEBP = ".webp"
    private const val PNG = ".png"
    private const val JEG = ".jpg"
    fun getBitmap(file: File?): Bitmap? {
        return if (file == null) null else BitmapFactory.decodeFile(file.absolutePath)
    }

    fun getBitmap(filePath: String?): Bitmap? {
        return if (TextUtils.isEmpty(filePath)) null else BitmapFactory.decodeFile(filePath)
    }

    @JvmOverloads
    fun scale(src: Bitmap, newWidth: Int, newHeight: Int, recycle: Boolean = false): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    @JvmOverloads
    fun scale(
        src: Bitmap,
        scaleWidth: Float,
        scaleHeight: Float,
        recycle: Boolean = false
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val matrix = Matrix()
            matrix.setScale(scaleWidth, scaleHeight)
            val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    fun scale(
        src: Bitmap,
        scaleWidth: Float,
        scaleHeight: Float,
        px: Float,
        py: Float,
        recycle: Boolean
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val matrix = Matrix()
            matrix.setScale(scaleWidth, scaleHeight, px, py)
            val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    @JvmOverloads
    fun clip(
        src: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        recycle: Boolean = false
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val ret = Bitmap.createBitmap(src, x, y, width, height)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    fun skew(src: Bitmap, kx: Float, ky: Float, recycle: Boolean): Bitmap? {
        return skew(src, kx, ky, 0.0f, 0.0f, recycle)
    }

    @JvmOverloads
    fun skew(
        src: Bitmap,
        kx: Float,
        ky: Float,
        px: Float = 0.0f,
        py: Float = 0.0f,
        recycle: Boolean = false
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val matrix = Matrix()
            matrix.setSkew(kx, ky, px, py)
            val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    @JvmOverloads
    fun rotate(src: Bitmap, degrees: Int, px: Float, py: Float, recycle: Boolean = false): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else if (degrees == 0) {
            src
        } else {
            val matrix = Matrix()
            matrix.setRotate(degrees.toFloat(), px, py)
            val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            ret
        }
    }

    fun getRotateDegree(filePath: String?): Int {
        return try {
            val exifInterface = ExifInterface(filePath!!)
            val orientation = exifInterface.getAttributeInt("Orientation", 1)
            when (orientation) {
                3 -> 180
                6 -> 90
                8 -> 270
                else -> 0
            }
        } catch (var3: IOException) {
            var3.printStackTrace()
            -1
        }
    }

    @JvmOverloads
    fun compressByQuality(
        src: Bitmap,
        @IntRange(from = 0L, to = 100L) quality: Int,
        recycle: Boolean = false
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val baos = ByteArrayOutputStream()
            src.compress(CompressFormat.JPEG, quality, baos)
            val bytes = baos.toByteArray()
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    @JvmOverloads
    fun compressByQuality(src: Bitmap, maxByteSize: Long, recycle: Boolean = false): Bitmap? {
        return if (!isEmptyBitmap(src) && maxByteSize > 0L) {
            var quality = 100
            val baos = ByteArrayOutputStream()
            src.compress(CompressFormat.JPEG, quality, baos)
            while (baos.toByteArray().size.toLong() > maxByteSize && quality > 0) {
                baos.reset()
                src.compress(CompressFormat.JPEG, quality, baos)
                quality -= 10
            }
            val bytes = baos.toByteArray()
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }

    fun getFitSampleBitmap(file_path: String?, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file_path, options)
        options.inSampleSize = calculateInSampleSize(options, width, height)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file_path, options)
    }

    @JvmOverloads
    fun compressBySampleSize(src: Bitmap, sampleSize: Int, recycle: Boolean = false): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val options = BitmapFactory.Options()
            options.inSampleSize = sampleSize
            val baos = ByteArrayOutputStream()
            src.compress(CompressFormat.JPEG, 100, baos)
            val bytes = baos.toByteArray()
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }

    @JvmOverloads
    fun compressBySampleSize(
        src: Bitmap,
        maxWidth: Int,
        maxHeight: Int,
        recycle: Boolean = false
    ): Bitmap? {
        return if (isEmptyBitmap(src)) {
            null
        } else {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val baos = ByteArrayOutputStream()
            src.compress(CompressFormat.JPEG, 100, baos)
            val bytes = baos.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            options.inSampleSize = calculateInSampleSize(
                options,
                maxWidth,
                maxHeight
            )
            options.inJustDecodeBounds = false
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }

    fun toRoundedCorner(bitmap: Bitmap?, radius: Float): Bitmap? {
        return if (bitmap == null) {
            null
        } else {
            val width = bitmap.width
            val height = bitmap.height
            toRoundedCorner(bitmap, width, height, radius)
        }
    }

    fun toRoundedCorner(path: String?, radius: Float): Bitmap? {
        val op = BitmapFactory.Options()
        op.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(path, op)
        return toRoundedCorner(bitmap, radius)
    }

    fun toRoundedCorner(bitmap: Bitmap, width: Int, height: Int, radius: Float): Bitmap? {
        return if (width > 0 && height > 0) {
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint()
            val color = -12434878
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val destRect = Rect(0, 0, width, height)
            val destRectF = RectF(destRect)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = -12434878
            canvas.drawRoundRect(destRectF, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, srcRect, destRect, paint)
            output
        } else {
            null
        }
    }

    /**
     * 从普通文件中读入图片
     * @param fileName
     * @return
     */
    fun getBitmapFromFile(fileName: String?): Bitmap? {
        val bitmap: Bitmap?
        val file = File(fileName)
        if (!file.exists()) {
            return null
        }
        bitmap = try {
            BitmapFactory.decodeFile(fileName)
        } catch (e: Exception) {
            Log.e("ShiTouren", "getBitmapFromFile: ", e)
            null
        }
        return bitmap
    }

    fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        var height = options.outHeight
        var width = options.outWidth
        var inSampleSize: Int
        inSampleSize = 1
        while (1.let { width = width shr it; width } >= maxWidth && 1.let {
                height = height shr it; height
            } >= maxHeight) {
            inSampleSize = inSampleSize shl 1
        }
        return inSampleSize
    }

    fun resize(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val ratioX = newWidth.toFloat() / bitmap.width.toFloat()
        val ratioY = newHeight.toFloat() / bitmap.height.toFloat()
        val middleX = newWidth.toFloat() / 2.0f
        val middleY = newHeight.toFloat() / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bitmap,
            middleX - (bitmap.width / 2).toFloat(),
            middleY - (bitmap.height / 2).toFloat(),
            Paint(2)
        )
        return scaledBitmap
    }

    fun checkBitmapFileSuffix(bitmapFileName: String, format: CompressFormat?): Boolean {
        var result = true
        when (format) {
            CompressFormat.PNG -> if (!bitmapFileName.endsWith(".png")) {
                result = false
            }
            CompressFormat.JPEG -> if (!bitmapFileName.endsWith(".jpg")) {
                result = false
            }
            CompressFormat.WEBP -> if (!bitmapFileName.endsWith(".webp")) {
                result = false
            }
        }
        return result
    }

    fun addBitmapFileSuffix(bitmapFileName: String, format: CompressFormat?): String {
        var bitmapFileName = bitmapFileName
        when (format) {
            CompressFormat.PNG -> if (!bitmapFileName.endsWith(".png")) {
                bitmapFileName = "$bitmapFileName.png"
            }
            CompressFormat.JPEG -> if (!bitmapFileName.endsWith(".jpg")) {
                bitmapFileName = "$bitmapFileName.jpg"
            }
            CompressFormat.WEBP -> if (!bitmapFileName.endsWith(".webp")) {
                bitmapFileName = "$bitmapFileName.webp"
            }
        }
        return bitmapFileName
    }

    /**
     * 加载Assets文件夹下的图片
     * @param context
     * @param fileName
     * @return
     */
    fun getImageFromAssetsFile(context: Context, fileName: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val manager = context.resources.assets
        try {
            val `is` = manager.open(fileName!!)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }
}
