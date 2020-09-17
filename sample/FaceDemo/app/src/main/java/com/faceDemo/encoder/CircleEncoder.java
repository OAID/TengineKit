package com.faceDemo.encoder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


import com.tenginekit.model.TenginekitPoint;

import java.util.ArrayList;
import java.util.List;

public class CircleEncoder extends DrawEncoder {
    private boolean drawCircle = true;
    private int frameWidth;
    private int frameHeight;

    private final Paint circlePaint = new Paint();
    private List<List<TenginekitPoint>> trackedObjects = new ArrayList<>();

    public CircleEncoder(final Context context) {
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStrokeWidth((float) 2.0);
        circlePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public synchronized void setFrameConfiguration(final int width, final int height) {
        if (!drawCircle) return;
        frameWidth = width;
        frameHeight = height;
    }

    int count = 0;

    @Override
    public synchronized void draw(final Canvas canvas) {
        if (!drawCircle || trackedObjects == null || trackedObjects.size() <= 0) {
            return;
        }
        for (int i = 0; i < trackedObjects.size(); i++) {
            for (int j = 0; j < trackedObjects.get(i).size() ; j++) {
                float x = 0;
                float y = 0;
                x = trackedObjects.get(i).get(j).X;
                y = trackedObjects.get(i).get(j).Y;
                canvas.drawCircle(x, y, 2, circlePaint);
            }
        }
    }

    @Override
    public synchronized void processResults(Object results) {
        if (!drawCircle || results == null) return;
        if (results.getClass() == ArrayList.class) {
            trackedObjects = (List<List<TenginekitPoint>>) results;
        }
    }

}
