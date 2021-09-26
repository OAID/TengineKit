/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.tenginekit.tenginedemo.currencyview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.tenginekit.tenginedemo.encoder.DrawEncoder;
import com.tenginekit.tenginedemo.encoder.EncoderBus;

/**
 * A simple View providing a render callback to other classes.
 */
public class OverlayView extends View {
    private boolean clear = false;
    private EncoderBus encoderBus;

    public OverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        encoderBus = new EncoderBus();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        encoderBus = new EncoderBus();
    }

    @Override
    public synchronized void draw(final Canvas canvas) {
        if (clear) {
            canvas.drawColor(Color.TRANSPARENT);
            clear = false;
            return;
        }
        encoderBus.onDraw(canvas);
    }

    public void clearEncoder() {
        clear = true;
        if (encoderBus != null){
            encoderBus.onClear();
        }
    }

    public void register(DrawEncoder encoder) {
        if (encoderBus != null) {
            encoderBus.Registe(encoder);
        }
    }

    public void unRegisterAll(){
        encoderBus.unRegisterAll();
    }

    public void unRegister(DrawEncoder encoder) {
        encoderBus.UnRegiste(encoder);
    }

    public void onProcessResults(Object object) {
        if (encoderBus != null) {
            encoderBus.onProcessResults(object);
        }
    }
}
