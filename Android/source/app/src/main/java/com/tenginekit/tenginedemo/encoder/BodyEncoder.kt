package com.tenginekit.tenginedemo.encoder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.tenginekit.engine.common.TenginekitPoint
import java.util.ArrayList

class BodyEncoder(context: Context) : DrawEncoder() {

//    colors =
//    [[255, 0, 0],  [255, 85, 0],   [255, 170, 0], [255, 255, 0],
//    [170, 255, 0], [85, 255, 0],   [0, 255, 0],   [0, 255, 85],
//    [0, 255, 170], [0, 255, 255],  [0, 170, 255], [0, 85, 255],
//    [0, 0, 255],  [85, 0, 255],    [170, 0, 255], [255, 0, 255]]


    //    pairs = [[8, 9], [11, 12], [11, 10], [2, 1], [1, 0], [13, 14], [14, 15], [3, 4],
    //    [4, 5], [8, 7], [7, 6], [6, 2],
//    [6, 3], [8, 12], [8, 13]]


    // pairs.len == 15
    private var pairs = listOf<Pair<Int, Int>>(
        Pair(8, 9), Pair(11, 12), Pair(11, 10), Pair(2, 1),
        Pair(1, 0), Pair(13, 14), Pair(14, 15), Pair(3, 4),
        Pair(4, 5), Pair(8, 7), Pair(7, 6), Pair(6, 2),
        Pair(6, 3), Pair(8, 12), Pair(8, 13)
    )


    // len == 16 !!
    private var colors = listOf<Int>(
        0xffFF0000.toInt(), 0xffFF5500.toInt(), 0xffFFAA00.toInt(), 0xffFFFF00.toInt(),
        0xffAAFF00.toInt(), 0xff55FF00.toInt(), 0xff00ff00.toInt(), 0xff00ff55.toInt(),
        0xff00ffaa.toInt(), 0xff00ffff.toInt(), 0xff00aaff.toInt(), 0xffff55ff.toInt(),
        0xff0000ff.toInt(), 0xff5500ff.toInt(), 0xffaa00ff.toInt(), 0xffff00ff.toInt()
    )

    private var trackedObjects: List<List<TenginekitPoint>>? = ArrayList()


    var paints = mutableListOf<Paint>()
    var paintLines = mutableListOf<Paint>()

    init {
        for (i in 0..15) {
            val paint = Paint()
            paint.color = colors[i]
            paint.isAntiAlias = true
            paint.strokeWidth = 20.0.toFloat()
            paint.style = Paint.Style.STROKE
            paints.add(i, paint)
            val paintl = Paint()
            paintl.color = colors[i]
            paintl.isAntiAlias = true
            paintl.strokeWidth = 10.0.toFloat()
            paintl.style = Paint.Style.STROKE
            paintLines.add(i, paintl)
        }
    }


    override fun setFrameConfiguration(width: Int, height: Int) {

    }

    override fun draw(canvas: Canvas) {

        trackedObjects?.let {
            if (it.isEmpty()) {
                return
            }
            for (i in it.indices) {
                for (j in it[i].indices) {
                    var x = 0f
                    var y = 0f
                    x = it[i][j].X
                    y = it[i][j].Y
                    canvas.drawCircle(x, y, 2f, paints[j])
                }

                for ((index, pair) in pairs.withIndex()) {
                    val startX = it[i][pair.first].X
                    val endX = it[i][pair.second].X
                    val startY = it[i][pair.first].Y
                    val endY = it[i][pair.second].Y
                    canvas.drawLine(
                        startX, startY, endX, endY, paintLines[index]
                    )
                }
            }
        }

    }

    override fun processResults(results: Any?) {
        if (results == null) return
        if (results is List<*>) {
            trackedObjects = results as List<List<TenginekitPoint>>
        }
    }

    override fun clearResult() {
        trackedObjects = null
    }
}
