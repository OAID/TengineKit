package com.faceDemo.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.faceDemo.R;
import com.faceDemo.utils.FileHandler;
import com.tenginekit.AndroidConfig;
import com.tenginekit.KitCore;
import com.tenginekit.face.Face;
import com.tenginekit.face.FaceDetectInfo;
import com.tenginekit.face.FaceLandmarkInfo;
import com.tenginekit.model.TenginekitPoint;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class ClassifierActivity extends AppCompatActivity {

    private static final String TAG = "ClassifierActivity";
    ImageView showImage;
    private final Paint rectPaint = new Paint();
    private final Paint circlePaint = new Paint();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_classifier);
        FileHandler.copyAllAssets(this, "sdcard/OAL/");
        showImage = findViewById(R.id.show_image);
        run();
    }

    private void CanvasInit(){
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(2.0f);

        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStrokeWidth((float) 2.0);
        circlePaint.setStyle(Paint.Style.STROKE);
    }

    public void run() {
        CanvasInit();
        Bitmap bb = null;
        try {
            Drawable d = Drawable.createFromStream(getAssets().open("1.jpg"), null);
            showImage.setImageDrawable(d);
            bb = ((BitmapDrawable) d).getBitmap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        int Image_w = bb.getWidth();
        int Image_h = bb.getHeight();

        Bitmap out_bitmap = Bitmap.createBitmap(
                showImage.getDrawable().getIntrinsicWidth(),
                showImage.getDrawable().getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out_bitmap);

        /**
         * 初始化
         * */
        KitCore.init(
                this,
                AndroidConfig
                        .create()
                        .setNormalMode()
                        .setDefaultFunc()
                        .setInputImageFormat(AndroidConfig.ImageFormat.RGBA)
                        .setInputImageSize(Image_w, Image_h)
                        .setOutputImageSize((int) Image_w, (int) Image_h)
        );
        /**
         * 获取人脸信息
         */
        byte[] data = bitmap2Bytes(bb);
        Face.FaceDetect faceDetect = Face.detect(data);
        List<FaceDetectInfo> faceDetectInfos = new ArrayList<>();
        List<FaceLandmarkInfo> landmarkInfos = new ArrayList<>();
        if (faceDetect.getFaceCount() > 0) {
            faceDetectInfos = faceDetect.getDetectInfos();
            landmarkInfos = faceDetect.landmark2d();
        }
        canvas.drawBitmap(bb, 0,0 , null);

        Log.d("#####", "Face Num: " + faceDetectInfos.size());
        if (faceDetectInfos != null && faceDetectInfos.size() > 0) {

            List<List<TenginekitPoint>> face_landmarks = new ArrayList<>();
            for (int i = 0; i < faceDetectInfos.size(); i++) {
                Rect rect = new Rect();
                rect = faceDetectInfos.get(i).asRect();
                canvas.drawRect(rect, rectPaint);
                for (int j = 0; j < landmarkInfos.get(i).landmarks.size() ; j++) {
                    float x = landmarkInfos.get(i).landmarks.get(j).X;
                    float y = landmarkInfos.get(i).landmarks.get(j).Y;
                    canvas.drawCircle(x, y, 2, circlePaint);
                }
            }
        }
        showImage.setImageBitmap(out_bitmap);
        KitCore.release();
    }

    private byte[] bitmap2Bytes(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the
        return temp;
    }
}