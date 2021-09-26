package com.tenginekit.tenginedemo.facedemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.engine.face.FaceConfig
import com.tenginekit.engine.common.TenginekitPoint
import com.tenginekit.tenginedemo.currencyview.OverlayView
import com.tenginekit.tenginedemo.encoder.BitmapEncoder
import com.tenginekit.tenginedemo.encoder.CircleEncoder
import com.tenginekit.tenginedemo.encoder.RectEncoder
import com.tenginekit.tenginedemo.utils.ImageUtils
import java.util.ArrayList
import android.content.Intent

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.R


class FaceBitmapActivity : AppCompatActivity(), View.OnClickListener {

    private var isDetecting: Boolean = false
    private var bitmap: Bitmap? = null
    private var imageView: ImageView? = null
    private var overlayView: OverlayView? = null
    private var width = 0
    private var height = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_bitmap)

        imageView = findViewById<ImageView>(R.id.iv)
        val buttonStartDetect = findViewById<TextView>(R.id.startDetect)
        buttonStartDetect.setOnClickListener(this)

        val buttonPick = findViewById<TextView>(R.id.pick)
        buttonPick.setOnClickListener(this)

        overlayView = findViewById(R.id.face_overlay)

        try {
            val picStream = assets.open("man.jpg")
            bitmap = BitmapFactory.decodeStream(picStream)
            picStream.close()
            imageView?.setImageBitmap(bitmap)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        val sdkConfig = SdkConfig()
        TengineKitSdk.getInstance().initSdk(externalCacheDir!!.absolutePath, sdkConfig, this)
        TengineKitSdk.getInstance().initFaceDetect()
    }

    private fun register() {
        overlayView?.register(BitmapEncoder(this))
        overlayView?.register(CircleEncoder(this))
        overlayView?.register(RectEncoder(this))
    }


    override fun onResume() {
        super.onResume()
        prepareOverView()
        register()
    }

    private fun prepareOverView() {
        register()
        val param = overlayView?.layoutParams
        width = param?.width ?: 0
        height = param?.height ?: 0
        Log.i(Constant.LOG_TAG, "width: $width height: $height")
        val param1 = imageView?.layoutParams
        Log.i(Constant.LOG_TAG, "width1: ${param1?.width} height: ${param1?.height}")

        Log.i(
            Constant.LOG_TAG, "dp2px :${ImageUtils.dip2px(this, 300f)}  " +
                    "${ImageUtils.dip2px(this, 400f)}"
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.startDetect -> {
                doDetect()
            }
            R.id.pick -> {
                pickGallery()
            }
        }
    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    bitmap = ImageUtils.getCorrectlyOrientedImage(this, it.data)
                    imageView?.setImageBitmap(bitmap)
                }
            }
        }

    private fun pickGallery() {
        overlayView?.clearEncoder()
        overlayView?.invalidate()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun doDetect() {
        if (isDetecting) {
            Toast.makeText(this, "is detecting current now please wait", Toast.LENGTH_LONG).show()
        } else {
            isDetecting = true
            bitmap?.let {
                val byte = ImageUtils.bitmap2RGB(bitmap)
                val config = FaceConfig().apply {
                    detect = true
                    landmark2d = true
                    video = false
                }
                val imageConfig = ImageConfig().apply {
                    data = byte
                    degree = 0
                    mirror = false
                    height = it.height
                    width = it.width
                    format = ImageConfig.FaceImageFormat.RGB
                }
                val faces = TengineKitSdk.getInstance().detectFace(imageConfig, config)
                if (faces.isNotEmpty()) {
                    Log.i(Constant.LOG_TAG, "faces length:" + faces.size)
                    val faceRects = arrayOfNulls<Rect>(faces.size)
                    val faceLandmarks: MutableList<List<TenginekitPoint>> =
                        ArrayList<List<TenginekitPoint>>()
                    for ((i, face) in faces.withIndex()) {
                        val faceLandmarkList = mutableListOf<TenginekitPoint>()
                        for (j in 0..211) {
                            faceLandmarkList.add(
                                j,
                                TenginekitPoint(
                                    face.landmark[j * 2] * width,
                                    face.landmark[j * 2 + 1] * height
                                )
                            )
                        }
                        Log.i(Constant.LOG_TAG, face.toString())
                        val rect = Rect(
                            (face.x1 * width).toInt(),
                            (face.y1 * height).toInt(),
                            (face.x2 * width).toInt(),
                            (face.y2 * height).toInt()
                        )
                        faceLandmarks.add(i, faceLandmarkList)
                        faceRects[i] = rect
                    }
                    overlayView?.onProcessResults(faceRects)
                    overlayView?.onProcessResults(faceLandmarks)
                }
                Log.i(Constant.LOG_TAG, "end detect")
            }

            runOnUiThread {
                overlayView?.invalidate()
            }
            isDetecting = false
        }
    }

    override fun onDestroy() {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        super.onDestroy()
        releaseSdk()
        overlayView?.unRegisterAll()
    }

    private fun releaseSdk() {
        TengineKitSdk.getInstance().releaseFaceDetect()
        TengineKitSdk.getInstance().release()
    }

}
