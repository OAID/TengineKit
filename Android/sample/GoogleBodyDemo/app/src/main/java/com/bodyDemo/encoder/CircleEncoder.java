package com.bodyDemo.encoder;

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

    private final Paint paint1 = new Paint();
    private final Paint paint2 = new Paint();
    private final Paint paint3 = new Paint();
    private final Paint paint4 = new Paint();
    private List<List<TenginekitPoint>> trackedObjects = new ArrayList<>();

    public CircleEncoder(final Context context) {
        paint1.setAntiAlias(true);
        paint1.setColor(Color.GREEN);
        paint1.setStrokeWidth((float) 6.0);

        paint2.setAntiAlias(true);
        paint2.setColor(Color.BLUE);
        paint2.setStrokeWidth((float) 6.0);

        paint3.setAntiAlias(true);
        paint3.setColor(Color.YELLOW);
        paint3.setStrokeWidth((float) 6.0);

        paint4.setAntiAlias(true);
        paint4.setColor(Color.RED);
        paint4.setStrokeWidth((float) 6.0);

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
            canvas.drawLine(trackedObjects.get(i).get(11).X, trackedObjects.get(i).get(11).Y, trackedObjects.get(i).get(12).X, trackedObjects.get(i).get(12).Y,paint1);
            canvas.drawLine(trackedObjects.get(i).get(12).X, trackedObjects.get(i).get(12).Y, trackedObjects.get(i).get(24).X, trackedObjects.get(i).get(24).Y,paint1);
            canvas.drawLine(trackedObjects.get(i).get(24).X, trackedObjects.get(i).get(24).Y, trackedObjects.get(i).get(23).X, trackedObjects.get(i).get(23).Y,paint1);
            canvas.drawLine(trackedObjects.get(i).get(23).X, trackedObjects.get(i).get(23).Y, trackedObjects.get(i).get(11).X, trackedObjects.get(i).get(11).Y,paint1);

            canvas.drawLine(trackedObjects.get(i).get(11).X, trackedObjects.get(i).get(11).Y, trackedObjects.get(i).get(13).X, trackedObjects.get(i).get(13).Y,paint2);
            canvas.drawLine(trackedObjects.get(i).get(13).X, trackedObjects.get(i).get(13).Y, trackedObjects.get(i).get(15).X, trackedObjects.get(i).get(15).Y,paint2);
            canvas.drawLine(trackedObjects.get(i).get(15).X, trackedObjects.get(i).get(15).Y, trackedObjects.get(i).get(21).X, trackedObjects.get(i).get(21).Y,paint2);
            canvas.drawLine(trackedObjects.get(i).get(15).X, trackedObjects.get(i).get(15).Y, trackedObjects.get(i).get(19).X, trackedObjects.get(i).get(19).Y,paint2);
            canvas.drawLine(trackedObjects.get(i).get(15).X, trackedObjects.get(i).get(15).Y, trackedObjects.get(i).get(17).X, trackedObjects.get(i).get(17).Y,paint2);
            canvas.drawLine(trackedObjects.get(i).get(17).X, trackedObjects.get(i).get(17).Y, trackedObjects.get(i).get(19).X, trackedObjects.get(i).get(19).Y,paint2);

            canvas.drawLine(trackedObjects.get(i).get(12).X, trackedObjects.get(i).get(12).Y, trackedObjects.get(i).get(14).X, trackedObjects.get(i).get(14).Y,paint3);
            canvas.drawLine(trackedObjects.get(i).get(14).X, trackedObjects.get(i).get(14).Y, trackedObjects.get(i).get(16).X, trackedObjects.get(i).get(16).Y,paint3);
            canvas.drawLine(trackedObjects.get(i).get(16).X, trackedObjects.get(i).get(16).Y, trackedObjects.get(i).get(22).X, trackedObjects.get(i).get(22).Y,paint3);
            canvas.drawLine(trackedObjects.get(i).get(16).X, trackedObjects.get(i).get(16).Y, trackedObjects.get(i).get(20).X, trackedObjects.get(i).get(20).Y,paint3);
            canvas.drawLine(trackedObjects.get(i).get(16).X, trackedObjects.get(i).get(16).Y, trackedObjects.get(i).get(18).X, trackedObjects.get(i).get(18).Y,paint3);
            canvas.drawLine(trackedObjects.get(i).get(18).X, trackedObjects.get(i).get(18).Y, trackedObjects.get(i).get(20).X, trackedObjects.get(i).get(20).Y,paint3);

            canvas.drawLine(trackedObjects.get(i).get(9).X, trackedObjects.get(i).get(9).Y, trackedObjects.get(i).get(10).X, trackedObjects.get(i).get(10).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(0).X, trackedObjects.get(i).get(0).Y, trackedObjects.get(i).get(1).X, trackedObjects.get(i).get(1).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(1).X, trackedObjects.get(i).get(1).Y, trackedObjects.get(i).get(2).X, trackedObjects.get(i).get(2).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(2).X, trackedObjects.get(i).get(2).Y, trackedObjects.get(i).get(3).X, trackedObjects.get(i).get(3).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(3).X, trackedObjects.get(i).get(3).Y, trackedObjects.get(i).get(7).X, trackedObjects.get(i).get(7).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(0).X, trackedObjects.get(i).get(0).Y, trackedObjects.get(i).get(4).X, trackedObjects.get(i).get(4).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(4).X, trackedObjects.get(i).get(4).Y, trackedObjects.get(i).get(5).X, trackedObjects.get(i).get(5).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(5).X, trackedObjects.get(i).get(5).Y, trackedObjects.get(i).get(6).X, trackedObjects.get(i).get(6).Y,paint4);
            canvas.drawLine(trackedObjects.get(i).get(6).X, trackedObjects.get(i).get(6).Y, trackedObjects.get(i).get(8).X, trackedObjects.get(i).get(8).Y,paint4);

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
