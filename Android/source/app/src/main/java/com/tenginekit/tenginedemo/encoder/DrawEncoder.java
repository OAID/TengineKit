package com.tenginekit.tenginedemo.encoder;

import android.graphics.Canvas;

public abstract class DrawEncoder {
    abstract void setFrameConfiguration(final int width, final int height);

    abstract void draw(final Canvas canvas);

    abstract void processResults(Object object);

    public abstract void clearResult();
}
