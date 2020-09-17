package com.bodyDemo.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bodyDemo.R;
import com.bodyDemo.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String TAG = "MainActivity";
    private ImageView mEffectVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.detect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.detect:
                startVideoWithFaceDetected();
                break;
        }
    }

    private void startVideoWithFaceDetected() {
        PermissionUtils.checkPermission(this, new Runnable() {
            @Override
            public void run() {
                jumpToCameraActivity();
            }
        });
    }

    public void jumpToCameraActivity()
    {
        Intent intent = new Intent(MainActivity.this, ClassifierActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                jumpToCameraActivity();
            } else {
                startVideoWithFaceDetected();
            }
        }
    }
}
