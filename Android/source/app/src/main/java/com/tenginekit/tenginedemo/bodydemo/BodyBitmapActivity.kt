package com.tenginekit.tenginedemo.bodydemo


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.engine.body.BodyConfig
import com.tenginekit.engine.common.TenginekitPoint
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.databinding.ActivityBodyBitmapBinding
import com.tenginekit.tenginedemo.encoder.BitmapEncoder
import com.tenginekit.tenginedemo.encoder.BodyEncoder
import com.tenginekit.tenginedemo.encoder.RectEncoder
import com.tenginekit.tenginedemo.utils.DisplayUtils
import com.tenginekit.tenginedemo.utils.ImageUtils
import java.util.ArrayList

class BodyBitmapActivity : AppCompatActivity() {

    lateinit var binding: ActivityBodyBitmapBinding
    var bitmap: Bitmap? = null
    var overlayWidth = 0
    var overlayHeight = 0
    private val TAG = Constant.LOG_TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBodyBitmapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.detect.setOnClickListener {
            startDetect()
        }

        try {
            val picStream = assets.open("testseg1.jpeg")
            bitmap = BitmapFactory.decodeStream(picStream)
            picStream.close()
            binding.image.setImageBitmap(bitmap)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        val sdkConfig = SdkConfig()
        TengineKitSdk.getInstance().initSdk(externalCacheDir!!.absolutePath, sdkConfig, this)
        TengineKitSdk.getInstance().initBodyDetect()

    }


    override fun onResume() {
        super.onResume()
        initOverlayView()

    }


    override fun onDestroy() {
        super.onDestroy()
        TengineKitSdk.getInstance().releaseBodyDetect()
        TengineKitSdk.getInstance().release()
    }

    private fun initOverlayView() {


        binding.overlayView.post {
            overlayWidth = binding.overlayView.width
            overlayHeight = binding.overlayView.height

            Log.i(TAG, "$overlayHeight $overlayWidth")
            Log.i(TAG, "${DisplayUtils.dp2px(this, 300f)}")
        }

        binding.overlayView.register(BitmapEncoder(this))
        binding.overlayView.register(BodyEncoder(this))
        binding.overlayView.register(RectEncoder(this))
    }

    private fun startDetect() {
        bitmap?.let {
            val data = ImageUtils.bitmap2RGB(bitmap)
            val imageConfig = ImageConfig().apply {
                this.data = data
                this.format = ImageConfig.FaceImageFormat.RGB
                this.height = it.height
                this.width = it.width
                this.mirror = false
                this.degree = 0
            }
            val bodyConfig = BodyConfig()
            val bodyS = TengineKitSdk.getInstance().bodyDetect(imageConfig, bodyConfig)
            bodyS?.let { bodyS ->
                if (bodyS.isNotEmpty()) {
                    Log.i(Constant.LOG_TAG, "body length:" + bodyS.size)
                    val bodyRects = arrayOfNulls<Rect>(bodyS.size)
                    val faceLandmarks: MutableList<List<TenginekitPoint>> =
                        ArrayList<List<TenginekitPoint>>()
                    for ((i, body) in bodyS.withIndex()) {
                        val landmark = mutableListOf<TenginekitPoint>()
                        val rect = Rect(
                            (body.x1 * overlayWidth.toFloat()).toInt(),
                            (body.y1 * overlayHeight.toFloat()).toInt(),
                            (body.x2 * overlayWidth.toFloat()).toInt(),
                            (body.y2 * overlayHeight.toFloat()).toInt()
                        )
                        bodyRects[i] = rect
                        body.landmark?.let {
                            for (j in 0..15) {
                                landmark.add(
                                    j,
                                    TenginekitPoint(
                                        it[2 * j] * overlayWidth,
                                        it[2 * j + 1] * overlayHeight
                                    )
                                )
                            }
                        }
                        Log.i(TAG, "$rect")
                        faceLandmarks.add(i, landmark)
                    }
                    binding.overlayView.onProcessResults(bodyRects)
                    binding.overlayView.onProcessResults(faceLandmarks)
                }
            }
        }

        runOnUiThread {
            binding.overlayView.invalidate()
        }

    }

}
