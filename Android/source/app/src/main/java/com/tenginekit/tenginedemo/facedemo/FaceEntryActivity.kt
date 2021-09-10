package com.tenginekit.tenginedemo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.tenginedemo.facedemo.FaceBitmapActivity
import com.tenginekit.tenginedemo.facedemo.FaceVideoActivity
import com.tenginekit.tenginedemo.utils.ModelCopyCallback
import com.tenginekit.tenginedemo.utils.PermissionUtils
import com.tenginekit.tenginedemo.utils.copyAssetFolder

class FaceEntryActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val REQ_PERMISSION_CODE_BITMAP = 1
        const val REQ_PERMISSION_CODE_CAMERA = 2
    }

    private var modelCopyFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_face_entry)

        val jumpToBitmap = findViewById<TextView>(R.id.jumpToBitmap)
        val jumpToCamera = findViewById<TextView>(R.id.jumpToCamera)

        jumpToBitmap.setOnClickListener(this)
        jumpToCamera.setOnClickListener(this)
        //Thread { copyModel() }
        copyModel()
    }


    private fun copyModel() {
        assets.copyAssetFolder(
            "model",
            externalCacheDir?.absolutePath ?: "",
            object : ModelCopyCallback {
                override fun copyFinish() {
                    Log.i(Constant.LOG_TAG, "copyModel Success")
                    modelCopyFinished = true
                }

                override fun copyFail() {
                    Log.i(Constant.LOG_TAG, "copyModel fail")
                }
            })
    }

    private fun jumpToBitmapActivity() {
        val intent = Intent(this, FaceBitmapActivity::class.java)
        startActivity(intent)
    }

    private fun jumpToCameraActivity() {
        val intent = Intent(this, FaceVideoActivity::class.java)
        startActivity(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMISSION_CODE_BITMAP || requestCode == REQ_PERMISSION_CODE_CAMERA) {
            var jump = false
            for (result in grantResults) {
                jump = result == PackageManager.PERMISSION_GRANTED
            }
            if (jump) {
                if (requestCode == REQ_PERMISSION_CODE_CAMERA) {
                    jumpToCameraActivity()
                } else {
                    jumpToBitmapActivity()
                }
            }
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.jumpToBitmap -> {
                if (checkModel()) {
                    PermissionUtils.checkPermission(
                        this, {
                            jumpToBitmapActivity()
                        }, REQ_PERMISSION_CODE_BITMAP
                    )
                } else {
                    Toast.makeText(this, "wait model copy finish", Toast.LENGTH_LONG).show()
                }
            }
            R.id.jumpToCamera -> {
                if (checkModel()) {
                    PermissionUtils.checkPermission(
                        this, {
                            jumpToCameraActivity()
                        }, REQ_PERMISSION_CODE_CAMERA
                    )
                } else {
                    Toast.makeText(this, "wait model copy finish", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
            }
        }
    }

    private fun checkModel(): Boolean {
        return modelCopyFinished
    }
}
