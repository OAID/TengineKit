package com.tenginekit.tenginedemo.encoder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import java.lang.Math.abs
import kotlin.collections.ArrayList

class Iris {
    fun trans(screenWidth: Float, screenHeight: Float): Iris {
        for (i in 0..14) {
            leftLandmark[3 * i] *= screenWidth
            leftLandmark[3 * i + 1] *= screenHeight

            rightLandmark[3 * i] *= screenWidth
            rightLandmark[3 * i + 1] *= screenHeight
        }
        for (i in 0..4) {
            leftIris[3 * i] *= screenWidth
            leftIris[3 * i + 1] *= screenHeight

            rightIris[3 * i] *= screenWidth
            rightIris[3 * i + 1] *= screenHeight
        }


        return this
    }

    var leftLandmark = FloatArray(16 * 3)
    var rightLandmark = FloatArray(16 * 3)
    var leftIris = FloatArray(5 * 3)
    var rightIris = FloatArray(5 * 3)
}


class IrisEncoder(context: Context) : DrawEncoder() {
    private val paintIris = Paint()
    private val paintLandmark = Paint()
    private val paintIrisLine = Paint()

    private var iris = arrayListOf<Iris>()

    init {
        paintIris.color = 0xffff0000.toInt()
        paintIris.isAntiAlias = true
        paintIris.strokeWidth = 4.0.toFloat()
        paintIris.style = Paint.Style.STROKE

        paintIrisLine.color = 0xffff0000.toInt()
        paintIrisLine.isAntiAlias = true
        paintIrisLine.strokeWidth = 2.0.toFloat()
        paintIrisLine.style = Paint.Style.STROKE

        paintLandmark.color = 0xff0000ff.toInt()
        paintLandmark.isAntiAlias = true
        paintLandmark.strokeWidth = 2.0.toFloat()
        paintLandmark.style = Paint.Style.STROKE
    }

    override fun setFrameConfiguration(width: Int, height: Int) {

    }

    override fun draw(canvas: Canvas?) {
        for (ir in iris) {
            ir.let {
                for (i in 0..13) {
                    val endIndex = i + 1
                    canvas?.drawLine(
                        it.leftLandmark[3 * i],
                        it.leftLandmark[3 * i + 1],
                        it.leftLandmark[3 * endIndex],
                        it.leftLandmark[3 * endIndex + 1],
                        paintLandmark
                    )

                    canvas?.drawLine(
                        it.rightLandmark[3 * i],
                        it.rightLandmark[3 * i + 1],
                        it.rightLandmark[3 * endIndex],
                        it.rightLandmark[3 * endIndex + 1],
                        paintLandmark
                    )
                }
                canvas?.drawLine(
                    it.leftLandmark[3 * 9],
                    it.leftLandmark[3 * 9 + 1],
                    it.leftLandmark[0],
                    it.leftLandmark[1],
                    paintLandmark
                )

                canvas?.drawLine(
                    it.rightLandmark[3 * 9],
                    it.rightLandmark[3 * 9 + 1],
                    it.rightLandmark[0],
                    it.rightLandmark[1],
                    paintLandmark
                )

                canvas?.drawLine(
                    it.leftLandmark[3 * 14],
                    it.leftLandmark[3 * 14 + 1],
                    it.leftLandmark[3 * 8],
                    it.leftLandmark[3 * 8 + 1],
                    paintLandmark
                )

                canvas?.drawLine(
                    it.rightLandmark[3 * 14],
                    it.rightLandmark[3 * 14 + 1],
                    it.rightLandmark[3 * 8],
                    it.rightLandmark[3 * 8 + 1],
                    paintLandmark
                )

                val radLeft = kotlin.math.abs((it.leftIris[3] - it.leftIris[3 * 3]) / 2)
                canvas?.drawCircle(it.leftIris[0], it.leftIris[1], radLeft, paintIrisLine)

                val radRight = kotlin.math.abs((it.rightIris[3] - it.rightIris[3 * 3]) / 2)
                canvas?.drawCircle(it.rightIris[0], it.rightIris[1], radRight, paintIrisLine)

                for (i in 0..4) {
                    canvas?.drawCircle(
                        it.leftIris[3 * i],
                        it.leftIris[3 * i + 1],
                        1.toFloat(),
                        paintIris
                    )
                    canvas?.drawCircle(
                        it.rightIris[3 * i],
                        it.rightIris[3 * i + 1],
                        1.toFloat(),
                        paintIris
                    )
                }
            }

        }
    }

    override fun processResults(res: Any?) {
        if (res is List<*>) {
            if (res.isNotEmpty() && res[0] is Iris) {
                iris = res as ArrayList<Iris>
            }
        }
    }

    override fun clearResult() {
    }
}
