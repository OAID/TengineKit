package com.tenginekit.tenginedemo.bodydemo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.R
import com.tenginekit.tenginedemo.databinding.ActivityBodyEntryBinding
import com.tenginekit.tenginedemo.facedemo.BodyFrameActivity
import com.tenginekit.tenginedemo.facedemo.FaceEntryActivity
import com.tenginekit.tenginedemo.utils.ModelCopyCallback
import com.tenginekit.tenginedemo.utils.PermissionUtils
import com.tenginekit.tenginedemo.utils.copyAssetFolder


class BodyEntryActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityBodyEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBodyEntryBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.bitmap.setOnClickListener(this)
        binding.camera.setOnClickListener(this)
        copyModel()


    }

    private fun copyModel() {
        assets.copyAssetFolder(
            "model",
            externalCacheDir?.absolutePath ?: "",
            object : ModelCopyCallback {
                override fun copyFinish() {
                    Log.i(Constant.LOG_TAG, "copyModel Success")
                }

                override fun copyFail() {
                    Log.i(Constant.LOG_TAG, "copyModel fail")
                }
            })
    }

    private fun jumpToBitmapActivity() {
        val intent = Intent(this, BodyBitmapActivity::class.java)
        startActivity(intent)
    }

    private fun jumpToCameraActivity() {
        val intent = Intent(this, BodyFrameActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FaceEntryActivity.REQ_PERMISSION_CODE_BITMAP || requestCode == FaceEntryActivity.REQ_PERMISSION_CODE_CAMERA) {
            var jump = false
            for (result in grantResults) {
                jump = result == PackageManager.PERMISSION_GRANTED
            }
            if (jump) {
                if (requestCode == FaceEntryActivity.REQ_PERMISSION_CODE_CAMERA) {
                    jumpToCameraActivity()
                } else {
                    jumpToBitmapActivity()
                }
            }
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bitmap -> {
                PermissionUtils.checkPermission(
                    this, {
                        jumpToBitmapActivity()
                    }, FaceEntryActivity.REQ_PERMISSION_CODE_BITMAP
                )
            }
            R.id.camera -> {
                PermissionUtils.checkPermission(
                    this, {
                        jumpToCameraActivity()
                    }, FaceEntryActivity.REQ_PERMISSION_CODE_CAMERA
                )
            }
            else -> {

            }
        }
    }
}
