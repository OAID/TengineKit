package com.faceDemo.encoder;

import android.graphics.Canvas;

public abstract class DrawEncoder {
    abstract void setFrameConfiguration(final int width, final int height);

    abstract void draw(final Canvas canvas);

    abstract void processResults(Object object);
}
