package com.faceDemo.encoder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


import com.tenginekit.model.TenginekitPoint;

import java.util.ArrayList;
import java.util.List;

public class EyeEncoder extends DrawEncoder {
    private boolean drawCircle = true;
    private int frameWidth;
    private int frameHeight;

    private final Paint circlePaint = new Paint();
    private final Paint circlePaint2 = new Paint();
    private List<List<TenginekitPoint>> trackedObjects = new ArrayList<>();

    public EyeEncoder(final Context context) {
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.RED);
        circlePaint.setStrokeWidth((float) 2.0);

        circlePaint2.setAntiAlias(true);
        circlePaint2.setColor(Color.BLUE);
        circlePaint2.setStyle(Paint.Style.STROKE);
        circlePaint2.setStrokeWidth((float) 2.0);
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
        for (int i = 0; i < trackedObjects.size() / 2; i++) {
            float rad = Math.abs(trackedObjects.get(i * 2).get(1).X - trackedObjects.get(i * 2).get(3).X) / 2.0f;
            canvas.drawCircle(trackedObjects.get(i * 2).get(0).X, trackedObjects.get(i * 2).get(0).Y, rad, circlePaint2);
            for (int j = 0; j < trackedObjects.get(i * 2).size() ; j++) {
                float x = 0;
                float y = 0;
                x = trackedObjects.get(i * 2).get(j).X;
                y = trackedObjects.get(i * 2).get(j).Y;
                canvas.drawCircle(x, y, 2, circlePaint);
            }
            for (int j = 0; j < trackedObjects.get(i * 2 + 1).size() ; j++) {
                if ((j >= 0 && j < 8) ||(j >= 9 && j < 15))
                {
                    float x1 = trackedObjects.get(i * 2 + 1).get(j).X;
                    float y1 = trackedObjects.get(i * 2 + 1).get(j).Y;
                    float x2 = trackedObjects.get(i * 2 + 1).get(j + 1).X;
                    float y2 = trackedObjects.get(i * 2 + 1).get(j + 1).Y;

                    canvas.drawLine(x1, y1, x2, y2,circlePaint);
                }
                else if (j == 8)
                {
                    float x1 = trackedObjects.get(i * 2 + 1).get(9).X;
                    float y1 = trackedObjects.get(i * 2 + 1).get(9).Y;
                    float x2 = trackedObjects.get(i * 2 + 1).get(0).X;
                    float y2 = trackedObjects.get(i * 2 + 1).get(0).Y;

                    canvas.drawLine(x1, y1, x2, y2, circlePaint);
                }
                else if (j == 15)
                {
                    float x1 = trackedObjects.get(i * 2 + 1).get(15).X;
                    float y1 = trackedObjects.get(i * 2 + 1).get(15).Y;
                    float x2 = trackedObjects.get(i * 2 + 1).get(8).X;
                    float y2 = trackedObjects.get(i * 2 + 1).get(8).Y;

                    canvas.drawLine(x1, y1, x2, y2,circlePaint);
                }
                canvas.drawCircle(trackedObjects.get(i * 2 + 1).get(j).X, trackedObjects.get(i * 2 + 1).get(j).Y, 2, circlePaint);
            }
        }
    }

    @Override
    public synchronized void processResults(Object results, String tag) {
        if (!drawCircle || results == null) return;
        if (tag == "eye") {
            trackedObjects = (List<List<TenginekitPoint>>) results;
        }
    }

}
