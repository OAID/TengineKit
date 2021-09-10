package com.tenginekit.tenginedemo.seg

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

public class XfermodeBitmap : View {


    private var paint: Paint? = null

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }


    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private var src: Bitmap? = null
    private var mask: Bitmap? = null
    private var bac: Bitmap? = null
    private var mHeight: Int = 0
    private var mWidth: Int = 0

    public fun setBitmap(src: Bitmap, bac: Bitmap, mask: Bitmap) {
        this.src = src
        this.bac = bac
        this.mask = mask

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = measuredHeight
        mWidth = measuredWidth
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (bac == null) {
            return
        }

        bac?.let {
            val matrix1 = Matrix()
            matrix1.setScale(
                mWidth.toFloat() / it.width.toFloat(),
                mHeight.toFloat() / it.height.toFloat()
            )
            canvas?.drawBitmap(it, matrix1, paint)
        }

        val onlyBac = canvas?.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), paint)

        src?.let {
            val matrix1 = Matrix()
            matrix1.setScale(
                mWidth.toFloat() / it.width.toFloat(),
                mHeight.toFloat() / it.height.toFloat()
            )
            canvas?.drawBitmap(it, matrix1, paint)
        }

        paint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

        mask?.let {
            val matrix1 = Matrix()
            matrix1.setScale(
                mWidth.toFloat() / it.width.toFloat(),
                mHeight.toFloat() / it.height.toFloat()
            )
            canvas?.drawBitmap(it, matrix1, paint)
        }


        paint?.xfermode = null

        onlyBac?.let { canvas.restoreToCount(it) }

    }


}
