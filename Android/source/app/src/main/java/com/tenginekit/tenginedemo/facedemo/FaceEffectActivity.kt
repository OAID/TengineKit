package com.tenginekit.tenginedemo.facedemo

import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.engine.face.FaceConfig
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.camera2.CameraV2Manager
import com.tenginekit.tenginedemo.databinding.ActivityFaceEffectBinding

class FaceEffectActivity : AppCompatActivity(), CameraV2Manager.FrameDataCallBack {
    private val TAG = Constant.LOG_TAG
    lateinit var binding: ActivityFaceEffectBinding

    private lateinit var cameraV2Manager: CameraV2Manager
    private lateinit var previewSize: Size
    private var renderer: FaceEffectRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceEffectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewSize = Size(0, 0)
        renderer = FaceEffectRenderer()
        cameraV2Manager = CameraV2Manager(
            this, true, previewSize,
            binding.surface, this, renderer
        )

        TengineKitSdk.getInstance().initSdk(externalCacheDir!!.absolutePath, SdkConfig(), this)
        TengineKitSdk.getInstance().initFaceDetect()
    }

    override fun onResume() {
        super.onResume()
        cameraV2Manager.openCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraV2Manager.releaseCamera()
        TengineKitSdk.getInstance().releaseFaceDetect()
        TengineKitSdk.getInstance().release()
    }


    override fun onFrameData(data: ByteArray?, width: Int, height: Int) {
        val faceConfig = FaceConfig().apply {
            detect = true
            landmark2d = true
        }
        val imageConfig = ImageConfig().apply {
            degree = 270
            this.data = data
            this.width = width
            this.height = height
            this.format = ImageConfig.FaceImageFormat.YUV
            this.mirror = true
        }
        val faces = TengineKitSdk.getInstance().detectFace(imageConfig, faceConfig)
        faces?.let {
            Log.i(TAG, "detect ${faces.size} faces")
        }
        renderer?.onLandmark(faces)
    }
}
