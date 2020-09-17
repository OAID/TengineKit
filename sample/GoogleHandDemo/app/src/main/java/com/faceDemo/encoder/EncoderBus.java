package com.faceDemo.encoder;

import android.graphics.Canvas;

import java.util.ArrayList;

public class EncoderBus {
    public static EncoderBus instace = new EncoderBus();
    private static ArrayList<DrawEncoder> encoders = new ArrayList<>();
    private static ArrayList<Class> encodersClass = new ArrayList<>();

    public static EncoderBus GetInstance() {
        return instace;
    }

    public void Registe(DrawEncoder encoder) {
        if (!encodersClass.contains(encoder.getClass())) {
            encoders.add(encoder);
            encodersClass.add(encoder.getClass());
        }
    }

    public void UnRegiste(DrawEncoder encoder) {
        if (encodersClass.contains(encoder.getClass())) {
             int index = encodersClass.indexOf(encoders.getClass());
             encodersClass.remove(index);
             encoders.remove(index);
        }
    }

    public void onSetFrameConfiguration(final int width, final int height) {
        for (int i = 0; i < encoders.size(); i++) {
            encoders.get(i).setFrameConfiguration(width, height);
        }
    }

    public void onDraw(final Canvas canvas) {
        for (int i = 0; i < encoders.size(); i++) {
            encoders.get(i).draw(canvas);
        }
    }

    public void onProcessResults(Object object) {
        for (int i = 0; i < encoders.size(); i++) {
            encoders.get(i).processResults(object);
        }
    }
}
