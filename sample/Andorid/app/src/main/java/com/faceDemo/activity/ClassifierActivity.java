package com.faceDemo.activity;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;

import com.faceDemo.R;
import com.faceDemo.camera.CameraEngine;
import com.faceDemo.currencyview.OverlayView;
import com.faceDemo.encoder.BitmapEncoder;
import com.faceDemo.encoder.CircleEncoder;
import com.faceDemo.encoder.EncoderBus;
import com.faceDemo.encoder.RectEncoder;
import com.tenginekit.Face;
import com.tenginekit.model.FaceDetectInfo;
import com.tenginekit.model.FaceLandmarkInfo;
import com.tenginekit.model.FaceLandmarkPoint;

import java.util.ArrayList;
import java.util.List;


public class ClassifierActivity extends CameraActivity {

    private static final String TAG = "ClassifierActivity";

    private OverlayView trackingOverlay;

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return new Size(1280, 960);
    }

    public void Registe() {
        /**
         * canvas 绘制人脸框，人脸关键点
         * */
        EncoderBus.GetInstance().Registe(new BitmapEncoder(this));
        EncoderBus.GetInstance().Registe(new CircleEncoder(this));
        EncoderBus.GetInstance().Registe(new RectEncoder(this));
    }

    @Override
    public void onPreviewSizeChosen(final Size size) {
        Registe();
        EncoderBus.GetInstance().onSetFrameConfiguration(previewHeight, previewWidth);

        trackingOverlay = (OverlayView) findViewById(R.id.facing_overlay);
        trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
                EncoderBus.GetInstance().onDraw(canvas);
            }
        });
    }

    @Override
    protected void processImage() {
        if (sensorEventUtil!= null) {
            getCameraBytes();
            Log.d("#####", "processImage: enter");
            int degree = CameraEngine.getInstance().getCameraOrientation(sensorEventUtil.orientation);
            /**
             * 设置旋转角
             */
            Face.Camera.setRotation(degree - 90, false, (int) CameraActivity.ScreenWidth, (int) CameraActivity.ScreenHeight);

            /**
             * 获取人脸信息
             */
            Face.FaceDetect faceDetect = Face.detect(mNV21Bytes);
            List<FaceDetectInfo> faceDetectInfos = new ArrayList<>();
            List<FaceLandmarkInfo> landmarkInfos = new ArrayList<>();
            if (faceDetect.getFaceCount() > 0) {
                faceDetectInfos = faceDetect.getDetectInfos();
                landmarkInfos = faceDetect.landmark2d();
            }
            Log.d("#####", "processImage: " + faceDetectInfos.size());
            if (faceDetectInfos != null && faceDetectInfos.size() > 0) {
                Rect[] face_rect = new Rect[faceDetectInfos.size()];

                List<List<FaceLandmarkPoint>> face_landmarks = new ArrayList<>();
                for (int i = 0; i < faceDetectInfos.size(); i++) {
                    Rect rect = new Rect();
                    rect = faceDetectInfos.get(i).asRect();
                    face_rect[i] = rect;
                    face_landmarks.add(landmarkInfos.get(i).landmarks);
                }
                EncoderBus.GetInstance().onProcessResults(face_rect);
                EncoderBus.GetInstance().onProcessResults(face_landmarks);
            }
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                readyForNextImage();
                if (trackingOverlay!=null) {
                    trackingOverlay.postInvalidate();
                }
            }
        });
    }
}