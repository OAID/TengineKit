package com.bodyDemo.activity;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;

import com.bodyDemo.R;
import com.bodyDemo.camera.CameraEngine;
import com.bodyDemo.currencyview.OverlayView;
import com.bodyDemo.encoder.BitmapEncoder;
import com.bodyDemo.encoder.CircleEncoder;
import com.bodyDemo.encoder.EncoderBus;
import com.bodyDemo.encoder.RectEncoder;
import com.tenginekit.KitCore;
import com.tenginekit.body.Body;
import com.tenginekit.body.BodyDetectInfo;
import com.tenginekit.body.BodyLandmarkInfo;
import com.tenginekit.model.TenginekitPoint;

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
            int degree = CameraEngine.getInstance().getCameraOrientation(sensorEventUtil.orientation);
            /**
             * 设置旋转角
             */
            KitCore.Camera.setRotation(degree - 90, false, (int) CameraActivity.ScreenWidth, (int) CameraActivity.ScreenHeight);

            /**
             * 获取人脸信息
             */
            Body.BodyDetect bodyDetect = Body.detect(mNV21Bytes);
            List<BodyDetectInfo> bodyDetectInfos = new ArrayList<>();
            List<BodyLandmarkInfo> landmarkInfos = new ArrayList<>();
            if (bodyDetect.getBodyCount() > 0) {
                bodyDetectInfos = bodyDetect.getDetectInfos();
                landmarkInfos = bodyDetect.landmark2d();
            }
            Log.d("#####", "Body Size: " + bodyDetectInfos.size());
            if (bodyDetectInfos != null && bodyDetectInfos.size() > 0) {
                Rect[] body_rect = new Rect[bodyDetectInfos.size()];

                List<List<TenginekitPoint>> body_landmarks = new ArrayList<>();
                for (int i = 0; i < bodyDetectInfos.size(); i++) {
                    Rect rect = new Rect();
                    rect = bodyDetectInfos.get(i).asRect();
                    body_rect[i] = rect;
                    body_landmarks.add(landmarkInfos.get(i).landmarks);
                }
                EncoderBus.GetInstance().onProcessResults(body_rect);
                EncoderBus.GetInstance().onProcessResults(body_landmarks);
            }
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                if (trackingOverlay!=null) {
                    trackingOverlay.postInvalidate();
                }
            }
        });
    }
}