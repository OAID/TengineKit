package com.tenginekit.tenginedemo.segdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.engine.seg.SegConfig
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.camera2.CameraV2Manager
import com.tenginekit.tenginedemo.databinding.ActivitySegVideoBinding
import com.tenginekit.tenginedemo.utils.DisplayUtils

class SegCameraActivity : AppCompatActivity(), CameraV2Manager.FrameDataCallBack {

    private lateinit var cameraManager: CameraV2Manager
    lateinit var binding: ActivitySegVideoBinding
    private var TAG = Constant.LOG_TAG
    private var previewSize: Size? = null
    private var testBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySegVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewSize = Size(
            DisplayUtils.dp2px(this, 300f),
            DisplayUtils.dp2px(this, 400f)
        )

        Log.i("ShiTouren", "${previewSize?.height}   ${previewSize?.width}")

        val picsTream1 = assets.open("bac.png")
        testBitmap = BitmapFactory.decodeStream(picsTream1)
        picsTream1.close()

        cameraManager = CameraV2Manager(
            this, true, previewSize, binding.glSurfaceView,
            this
        )

        val sdkConfig = SdkConfig()
        TengineKitSdk.getInstance().initSdk(externalCacheDir?.absolutePath, sdkConfig, this)
        TengineKitSdk.getInstance().initSegBody()
    }

    override fun onResume() {
        super.onResume()
        cameraManager.openCamera()
    }

    override fun onPause() {
        cameraManager.releaseCamera()
        super.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraManager.releaseCamera()
        TengineKitSdk.getInstance().releaseSegBody()
        TengineKitSdk.getInstance().release()
    }

    override fun onFrameData(data: ByteArray?, width: Int, height: Int) {
        Log.i(TAG, "onFrameData  width${width}  height${height}")
        val imageConfig = ImageConfig().apply {
            degree = 270
            mirror = true
            this.width = width
            this.height = height
            this.data = data
            this.format = ImageConfig.FaceImageFormat.YUV
        }
        val segConfig = SegConfig()
        val res = TengineKitSdk.getInstance().segHuman(imageConfig, segConfig)
        cameraManager.updateSegRes(res)
    }


}
