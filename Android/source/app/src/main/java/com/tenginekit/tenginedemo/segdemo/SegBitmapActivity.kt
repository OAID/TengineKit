package com.tenginekit.tenginedemo.segdemo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.engine.core.ImageConfig
import com.tenginekit.engine.core.SdkConfig
import com.tenginekit.engine.core.TengineKitSdk
import com.tenginekit.engine.seg.SegConfig
import com.tenginekit.tenginedemo.R
import com.tenginekit.tenginedemo.utils.ImageUtils

class SegBitmapActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageSegMask: ImageView
    private lateinit var modeResult: XfermodeBitmap
    private var bitmap: Bitmap? = null
    private var bitmapBac: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LayoutInflater.from(this).inflate(R.layout.seg_bitmap, null, false) as ViewGroup
        setContentView(root)


        val choose = root.findViewById<TextView>(R.id.choose)
        choose.setOnClickListener {
            pickGallery()
        }

        val seg = root.findViewById<TextView>(R.id.seg)
        seg.setOnClickListener {
            startSeg()
        }

//        setContentView(R.layout.seg_bitmap)
//        bitmapTextureView = GLBitmap(this)
//        val width = DisplayUtils.dp2px(this, 400f)
//        val height = DisplayUtils.dp2px(this, 400f)
//        root.addView(bitmapTextureView, width, height)

        imageSegMask = root.findViewById(R.id.regMask)
        imageView = root.findViewById(R.id.segImageView)
        modeResult = root.findViewById(R.id.modeRes)
        try {
            val picStream = assets.open("kobe.png")
            bitmap = BitmapFactory.decodeStream(picStream)
            picStream.close()


            val picsTream1 = assets.open("bac.png")
            bitmapBac = BitmapFactory.decodeStream(picsTream1)
            picsTream1.close()




            imageView.setImageBitmap(bitmap)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        val sdkConfig = SdkConfig().apply {
            sdkFunction = SdkConfig.SdkFunction.SEG
        }
        TengineKitSdk.getInstance().initSdk(externalCacheDir!!.absolutePath, sdkConfig, this)

    }

    private fun startSeg() {
        bitmap?.let {
            val byte = ImageUtils.bitmap2RGB(bitmap)
            val config = SegConfig()
            val imageConfig = ImageConfig().apply {
                data = byte
                degree = 0
                mirror = false
                height = it.height
                width = it.width
                format = ImageConfig.FaceImageFormat.RGB
            }
            val bitmapMask = TengineKitSdk.getInstance().segHuman(imageConfig, config)
            imageSegMask.setImageBitmap(bitmapMask)

            bitmapBac?.let { bit ->
                modeResult.setBitmap(it, bit, bitmapMask)
            }
        }

    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    this.bitmap = ImageUtils.getCorrectlyOrientedImage(this, it.data)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }

    private fun pickGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }
}
