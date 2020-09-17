package com.faceDemo.encoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

public class BitmapEncoder extends DrawEncoder {
    private boolean drawBitmap = true;
    private int frameWidth;
    private int frameHeight;

    private Matrix frameToCanvasMatrix;
    private final Paint rectPaint = new Paint();
    private Bitmap trackedObjects;

    public BitmapEncoder(final Context context) {
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(2.0f);
    }

    @Override
    public synchronized void setFrameConfiguration(final int width, final int height) {
        if (!drawBitmap) return;
        frameWidth = width;
        frameHeight = height;
    }

    @Override
    public synchronized void draw(final Canvas canvas) {
        if (!drawBitmap) return;
        if (trackedObjects == null) return;
        canvas.drawBitmap(trackedObjects, 10, 10, rectPaint);
    }

    @Override
    public synchronized void processResults(Object bitmap) {
        if (!drawBitmap || bitmap == null) return;
        if (bitmap.getClass() == Bitmap.class){
            trackedObjects = (Bitmap) bitmap;
        }
    }
}
