package com.tenginekit.tenginedemo.insightface

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
import com.tenginekit.engine.insightface.InsightFaceConfig
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.R


class InsightFaceBitmapActivity : AppCompatActivity(), View.OnClickListener {

    private var isDetecting: Boolean = false
    private var bitmap: Bitmap? = null
    private var imageView: ImageView? = null
    private var overlayView: OverlayView? = null
    private var width = 0
    private var height = 0
    private var regFeature: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insightface_bitmap)

        imageView = findViewById<ImageView>(R.id.iv)
        val buttonStartDetect = findViewById<TextView>(R.id.startDetect)
        buttonStartDetect.setOnClickListener(this)

        val buttonReg = findViewById<TextView>(R.id.startReg)
        buttonReg.setOnClickListener(this)

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
        TengineKitSdk.getInstance().initInsightFace()
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
            R.id.pick -> {
                pickGallery()
            }
            R.id.startReg -> {
                doRegister()
            }
            R.id.startDetect -> {
                doDetect()
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
        } else if (regFeature != null) {
            isDetecting = true
            bitmap?.let {
                val byte = ImageUtils.bitmap2RGB(bitmap)
                val config = InsightFaceConfig().apply {
                    scrfd = true
                    recognition = true
                    registered = true
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
                val faces = TengineKitSdk.getInstance().detectInsightFace(imageConfig, config)
                if (faces.isNotEmpty()) {
                    Log.i(Constant.LOG_TAG, "faces length:" + faces.size)
                    val faceRects = arrayOfNulls<Rect>(faces.size)
                    val faceLandmarks: MutableList<List<TenginekitPoint>> =
                        ArrayList<List<TenginekitPoint>>()
                    var faceFeature: FloatArray
                    for ((i, face) in faces.withIndex()) {
                        val faceLandmarkList = mutableListOf<TenginekitPoint>()
                        for (j in 0..4) {
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
                        faceFeature = face.feature
                        val distance = calculSimilar(regFeature!!, faceFeature)
                        Log.i(Constant.LOG_TAG, "Distance:" + distance.toString())
                        Log.i(Constant.LOG_TAG, "Confidence:" + face.confidence.toString())
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
        } else {
            val emptyNameToast =
                Toast.makeText(
                    this,
                    getString(R.string.InsightFaceRegError),
                    Toast.LENGTH_LONG
                )
            emptyNameToast.show()
        }
    }

    private fun doRegister() {
        if (isDetecting) {
            Toast.makeText(this, "is detecting current now please wait", Toast.LENGTH_LONG).show()
        } else {
            isDetecting = true
            bitmap?.let {
                val byte = ImageUtils.bitmap2RGB(bitmap)
                val config = InsightFaceConfig().apply {
                    scrfd = true
                    recognition = true
                    registered = false
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
                val faces = TengineKitSdk.getInstance().detectInsightFace(imageConfig, config)
                if (faces.size == 1) {
                    Log.i(Constant.LOG_TAG, "faces length:" + faces.size)
                    val faceRects = arrayOfNulls<Rect>(faces.size)
                    val faceLandmarks: MutableList<List<TenginekitPoint>> =
                        ArrayList<List<TenginekitPoint>>()
                    var faceFeature: FloatArray
                    for ((i, face) in faces.withIndex()) {
                        val faceLandmarkList = mutableListOf<TenginekitPoint>()
                        for (j in 0..4) {
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
                        regFeature = face.feature
                    }
                    overlayView?.onProcessResults(faceRects)
                    overlayView?.onProcessResults(faceLandmarks)
                } else {
                    val emptyNameToast =
                        Toast.makeText(
                            this,
                            getString(R.string.InsightFaceError),
                            Toast.LENGTH_LONG
                        )
                    emptyNameToast.show()
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
        TengineKitSdk.getInstance().releaseInsightFace()
        TengineKitSdk.getInstance().release()
    }

    private fun calculSimilar(regFeature: FloatArray, verFeature: FloatArray): Float {
        if (regFeature.size != verFeature.size || regFeature.isEmpty() || verFeature.isEmpty()) {
            return -1f
        }
        var sum = 0f
        for (i in regFeature.indices) {
            sum += regFeature.get(i) * verFeature.get(i)
        }
        return sum
    }


}
