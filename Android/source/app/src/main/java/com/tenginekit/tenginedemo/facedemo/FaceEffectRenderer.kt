package com.tenginekit.tenginedemo.facedemo

import com.tenginekit.engine.face.Face
import com.tenginekit.tenginedemo.camera2.CameraV2BaseRenderer
import com.tenginekit.tenginedemo.facedemo.effect.beauty.*
import com.tenginekit.tenginedemo.facedemo.effect.reshpae.FaceReshapeFilter
import com.tenginekit.tenginedemo.facedemo.effect.debug.FaceLandmarkFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FaceEffectRenderer : CameraV2BaseRenderer() {

    var beautyComplexionFilter: BeautyComplexionFilter? = null
    var beautySourceBlurFilter: BeautyXYBlurFilter? = null
    var beautyHighPassBlurFilter: BeautyXYBlurFilter? = null
    private var beautyHighPassFilter: BeautyHighPassFilter? = null
    var beautyAdjustFilter: BeautyAdjustFilter? = null

    var faceLandmarkFilter: FaceLandmarkFilter? = null

    var faceReshapeFilter: FaceReshapeFilter? = null

    var lastFaces: Array<Face>? = null

    override fun onDrawFrame(gl: GL10) {
        super.onDrawFrame(gl)

        // camera to oes texture
        var upstreamTexture = cameraFilter?.onDrawFrameBuffer(transformMatrix)

        // lut filter
        beautyComplexionFilter?.upstreamTexture = upstreamTexture!!
        upstreamTexture = beautyComplexionFilter?.onDrawFrameBuffer(transformMatrix)
        val sourceTexture = upstreamTexture

        // source blur filter
        beautySourceBlurFilter?.upstreamTexture = upstreamTexture!!
        upstreamTexture = beautySourceBlurFilter?.onDrawFrameBuffer(transformMatrix)
        val blurTexture = upstreamTexture

        // high pass message need save
        beautyHighPassFilter?.inputTexture = sourceTexture!!
        beautyHighPassFilter?.blurTexture = upstreamTexture!!
        upstreamTexture = beautyHighPassFilter?.onDrawFrameBuffer(transformMatrix)

        // high pass then little blur
        beautyHighPassBlurFilter?.upstreamTexture = upstreamTexture!!
        upstreamTexture = beautyHighPassBlurFilter?.onDrawFrameBuffer(transformMatrix)

        // ok final blend highpassblur sourceblur source
        beautyAdjustFilter?.highPassBlurTexture = upstreamTexture!!
        beautyAdjustFilter?.upstreamTexture = sourceTexture
        beautyAdjustFilter?.blurTexture = blurTexture!!


        if (lastFaces == null) {
            beautyAdjustFilter?.onDrawFrameScreen(transformMatrix)
        } else {
            upstreamTexture = beautyAdjustFilter?.onDrawFrameBuffer(transformMatrix)
            faceReshapeFilter?.upstreamTexture = upstreamTexture!!
            faceReshapeFilter?.onDrawFrameScreen(transformMatrix)
        }

        faceLandmarkFilter?.onDrawFrameScreen(transformMatrix)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        beautyComplexionFilter?.onSurfaceChanged(width, height)

        beautySourceBlurFilter?.onSurfaceChanged(
            (width.toFloat() * 0.5).toInt(),
            (height.toFloat() * 0.5).toInt()
        )

        beautyHighPassFilter?.onSurfaceChanged(
            (width.toFloat() * 0.5).toInt(),
            (height.toFloat() * 0.5).toInt()
        )
        beautyHighPassBlurFilter?.onSurfaceChanged(
            (width.toFloat() * 0.5).toInt(),
            (height.toFloat() * 0.5).toInt()
        )
        beautyAdjustFilter?.onSurfaceChanged(width, height)
        faceLandmarkFilter?.onSurfaceChanged(width, height)
        faceReshapeFilter?.onSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        super.onSurfaceCreated(gl, config)
        context?.let {
            beautyComplexionFilter = BeautyComplexionFilter(it)
        }
        beautySourceBlurFilter = BeautyXYBlurFilter(5)
        beautyHighPassFilter = BeautyHighPassFilter()
        beautyHighPassBlurFilter = BeautyXYBlurFilter(2)
        beautyAdjustFilter = BeautyAdjustFilter()
        faceLandmarkFilter = FaceLandmarkFilter()

        faceReshapeFilter = FaceReshapeFilter()
    }

    fun onLandmark(faces: Array<Face>?) {
        lastFaces = faces
        faces?.let {
            faceLandmarkFilter?.onLandmark(faces[0].landmark)
            faceReshapeFilter?.onLandmark(faces[0].landmark)
        }
    }
}
