package com.tenginekit.tenginedemo.facedemo.effect.beauty

class BeautyXYBlurFilter(size: Int) {
    var upstreamTexture = -1

    private var beautyXBlurFilter: BeautyBlurFilter? = null
    private var beautyYBlurFilter: BeautyBlurFilter? = null

    fun onSurfaceChanged(width: Int, height: Int) {
        beautyXBlurFilter?.onSurfaceChanged(width, height)
        beautyYBlurFilter?.onSurfaceChanged(width, height)

        beautyYBlurFilter?.setTexelOffset(0f, 1f)
        beautyXBlurFilter?.setTexelOffset(1f, 0f)
    }

    init {
        beautyXBlurFilter = BeautyBlurFilter(size)
        beautyYBlurFilter = BeautyBlurFilter(size)
    }

    fun onDrawFrameBuffer(transformMatrix: FloatArray): Int {
        beautyXBlurFilter?.upstreamTexture = upstreamTexture
        val upstream = beautyXBlurFilter?.onDrawFrameBuffer(transformMatrix)
        beautyYBlurFilter?.upstreamTexture = upstream!!
        return beautyYBlurFilter?.onDrawFrameBuffer(transformMatrix)!!
    }

    fun onDrawFrameScreen(transformMatrix: FloatArray) {
        beautyXBlurFilter?.upstreamTexture = upstreamTexture
        val upstream = beautyXBlurFilter?.onDrawFrameBuffer(transformMatrix)
        beautyYBlurFilter?.upstreamTexture = upstream!!
        beautyYBlurFilter?.onDrawFrameScreen(transformMatrix)
    }


}
