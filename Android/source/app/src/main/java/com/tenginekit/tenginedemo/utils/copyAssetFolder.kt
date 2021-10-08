package com.tenginekit.tenginedemo.utils

import android.content.res.AssetManager
import com.luck.picture.lib.tools.PictureFileUtils.isFileExists
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun AssetManager.copyAssetFolder(
    srcName: String,
    dstName: String,
    callback: ModelCopyCallback?
): Boolean {
    return try {
        var result = true
        val fileList = this.list(srcName) ?: return false
        if (fileList.isEmpty()) {
            result = copyAssetFile(srcName, dstName)
        } else {
            val file = File(dstName)
            result = file.mkdirs()
            for (filename in fileList) {
                result = result and copyAssetFolder(
                    srcName + separator.toString() + filename,
                    dstName + separator.toString() + filename,
                    null
                )
            }
        }
        callback?.copyFinish()
        result
    } catch (e: IOException) {
        e.printStackTrace()
        callback?.copyFail()
        false
    }
}

fun AssetManager.copyAssetFile(srcName: String, dstName: String): Boolean {
    return try {
        if (!isFileExists(dstName)) {
            val inStream = this.open(srcName)
            val outFile = File(dstName)
            val out: OutputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }

            inStream.close()
            out.close()
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}


interface ModelCopyCallback {
    fun copyFinish()
    fun copyFail()
}
