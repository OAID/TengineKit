package com.tenginekit.tenginedemo.utils;

import static android.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.media.ExifInterface.ORIENTATION_ROTATE_90;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.tenginekit.tenginedemo.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ImageUtils {
    public static int getYUVByteSize(final int width, final int height) {
        final int ySize = width * height;
        final int uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2;

        return ySize + uvSize;
    }

    public static byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];
        int count = rgba.length / 4;
        for (int i = 0; i < count; i++) {
            pixels[i * 3] = rgba[i * 4];
            pixels[i * 3 + 1] = rgba[i * 4 + 1];
            pixels[i * 3 + 2] = rgba[i * 4 + 2];
        }
        return pixels;
    }

    public static byte[] bitmap2RGBA(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] rgba = buffer.array();
        return rgba;
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        InputStream inputStream;
        inputStream = context.getContentResolver().openInputStream(photoUri);
        ExifInterface exif = null;
        Bitmap srcBitmap = null;
        int rotate = 0;
        try {
            exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            inputStream.close();
            inputStream = context.getContentResolver().openInputStream(photoUri);
            srcBitmap = BitmapFactory.decodeStream(inputStream);
            if (orientation == ORIENTATION_ROTATE_90) {
                rotate = 90;
            } else if (orientation == ORIENTATION_ROTATE_270) {
                rotate = 270;
            }
            if (rotate > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                        srcBitmap.getHeight(), matrix, true);
            }
            inputStream.close();
        } catch (Exception exception) {
            Log.e(Constant.LOG_TAG, "error in getCorrectlyOrientedImage");
        }
        return srcBitmap;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
