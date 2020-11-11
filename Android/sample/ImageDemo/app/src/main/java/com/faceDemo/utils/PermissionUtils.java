package com.faceDemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    /**
     * 权限列表
     */
    private static String[] permissionList = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    /***
     * 权限请求结果code
     */
    public static final int PERMISSIONS_REQUEST = 1;

    public static boolean checkPermission(Activity mActivity, Runnable callback) {
        List<String> needPermission = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissionList.length; i++) {
                if (mActivity.checkSelfPermission(permissionList[i]) != PackageManager.PERMISSION_GRANTED) {
                    needPermission.add(permissionList[i]);
                }
            }
            if (!needPermission.isEmpty()) {
                String[] permissions = needPermission.toArray(new String[needPermission.size()]);
                ActivityCompat.requestPermissions(mActivity,permissions,1);
                return false;
            }
            callback.run();
            return true;
        } else {
            callback.run();
            return true;
        }
    }
}
