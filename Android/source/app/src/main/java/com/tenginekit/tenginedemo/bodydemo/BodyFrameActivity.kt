package com.tenginekit.tenginedemo.facedemo

import android.graphics.Rect
import android.util.Log
import android.util.Size
import com.tenginekit.engine.body.BodyConfig
import com.tenginekit.tenginedemo.CameraActivity
import com.tenginekit.tenginedemo.R
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.engine.common.TenginekitPoint
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.tenginedemo.Constant
import com.tenginekit.tenginedemo.camera.CameraEngine
import com.tenginekit.tenginedemo.currencyview.OverlayView
import com.tenginekit.tenginedemo.encoder.*
import com.tenginekit.tenginedemo.utils.SensorEventUtil
import java.util.ArrayList

class BodyFrameActivity : CameraActivity() {
    private var trackingOverlay: OverlayView? = null
    var isProcessImage = false
    override fun getLayoutId(): Int {
        return R.layout.layout_classifer
    }

    override fun getDesiredPreviewFrameSize(): Size {
        return Size(1280, 960)
    }

    private fun register() {
        trackingOverlay?.register(BitmapEncoder(this))
        trackingOverlay?.register(BodyEncoder(this))
    }

    public override fun onPreviewSizeChosen(size: Size) {
        trackingOverlay = findViewById(R.id.facing_overlay)
        register()
    }

    override fun initSdk() {
        /**
         * 初始化
         */
        val config = SdkConfig();
        TengineKitSdk.getInstance().initSdk(externalCacheDir!!.absolutePath, config, this)
        TengineKitSdk.getInstance().initBodyDetect()
        if (sensorEventUtil == null) {
            sensorEventUtil = SensorEventUtil(this)
        }
    }

    override fun releaseSdk() {
        TengineKitSdk.getInstance().releaseBodyDetect()
        TengineKitSdk.getInstance().release()
    }

    override fun processImage() {
        sensorEventUtil?.let {
            val rotateDegree = CameraEngine.getInstance().getRotateDegree(it.orientation)
            Log.i(
                Constant.LOG_TAG,
                "orientation: ${it.orientation}  preprocess rotate degree $rotateDegree  needMirror: $is_front_camera"
            )
            if (isProcessImage) {
                return
            }
            Log.e(Constant.LOG_TAG, "start detect")
            isProcessImage = true
            val config = BodyConfig()
            val imageConfig = ImageConfig().apply {
                data = mNV21Bytes
                degree = rotateDegree
                mirror = true
                height = previewHeight
                width = previewWidth
                format = ImageConfig.FaceImageFormat.YUV
            }
            val faces = TengineKitSdk.getInstance().bodyDetect(imageConfig, config)
            if (faces != null && faces.isNotEmpty()) {
                Log.i(Constant.LOG_TAG, "faces length:" + faces.size)
                val faceRects = arrayOfNulls<Rect>(faces.size)
                val landmarks: MutableList<List<TenginekitPoint>> = ArrayList()
                for ((i, face) in faces.withIndex()) {
                    Log.i(Constant.LOG_TAG, face.toString())
                    val rect = CameraEngine.getRectByOrientation(
                        face.x1,
                        face.y1,
                        face.x2,
                        face.y2,
                        ScreenWidth,
                        ScreenHeight,
                        it.orientation
                    )
                    faceRects[i] = rect
                    val landmark: MutableList<TenginekitPoint> = ArrayList()
                    for (j in 0..15) {
                        landmark.add(
                            TenginekitPoint(
                                face.landmark[2 * j],
                                face.landmark[2 * j + 1]
                            ).rotateByOrientation(it.orientation, ScreenWidth, ScreenHeight)
                        )
                    }
                    /*
                        Test Facial action
                     */
                    Log.e(Constant.LOG_TAG, "close")
                    landmarks.add(landmark)
                }
                trackingOverlay?.onProcessResults(faceRects)
                trackingOverlay?.onProcessResults(landmarks)
            } else {
                trackingOverlay?.clearEncoder()
            }
            Log.e(Constant.LOG_TAG, "end detect")
            isProcessImage = false
        }
        runInBackground {
            if (trackingOverlay != null) {
                trackingOverlay!!.postInvalidate()
            }
        }
    }
}
