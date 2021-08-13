package com.tenginekit.engine.utils;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static byte[] readLocalFile(Context context,String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open("models/" + fileName);
        byte[] data = toByteArray(inputStream);
        inputStream.close();
        return data;
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
