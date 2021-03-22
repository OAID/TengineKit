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

import com.tenginekit.KitCore;
import com.tenginekit.hand.Hand;
import com.tenginekit.hand.HandDetectInfo;
import com.tenginekit.hand.HandLandmarkInfo;
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
            Hand.HandDetect handDetect = Hand.detect(mNV21Bytes);
            List<HandDetectInfo> handDetectInfos = new ArrayList<>();
            List<HandLandmarkInfo> landmarkInfos = new ArrayList<>();
            if (handDetect.getHandCount() > 0) {
                handDetectInfos = handDetect.getDetectInfos();
                landmarkInfos = handDetect.landmark3d();
            }
            Log.d("#####", "Hand Size: " + handDetectInfos.size());
            if (handDetectInfos != null && handDetectInfos.size() > 0) {
                Rect[] hand_rect = new Rect[handDetectInfos.size()];

                List<List<TenginekitPoint>> hand_landmarks = new ArrayList<>();
                for (int i = 0; i < handDetectInfos.size(); i++) {
                    Rect rect = new Rect();
                    rect = handDetectInfos.get(i).asRect();
                    hand_rect[i] = rect;
                    hand_landmarks.add(landmarkInfos.get(i).landmarks);
                }
                EncoderBus.GetInstance().onProcessResults(hand_rect);
                EncoderBus.GetInstance().onProcessResults(hand_landmarks);
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