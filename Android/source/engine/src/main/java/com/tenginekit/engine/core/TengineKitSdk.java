package com.tenginekit.engine.core;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tenginekit.engine.SDKConstant;
import com.tenginekit.engine.face.Face;
import com.tenginekit.engine.face.FaceConfig;
import com.tenginekit.engine.report.HttpReport;
import com.tenginekit.engine.utils.DeviceUtil;

public class TengineKitSdk {
    private SdkConfig.SdkFunction sdkFunction;

    private TengineKitSdk() {

    }

    private static TengineKitSdk instance;
    private TengineKitEngine tengineKitEngine;
    private boolean hasInit = false;
    private boolean allowReport = true;

    public static TengineKitSdk getInstance() {
        if (instance == null) {
            instance = new TengineKitSdk();
        }
        return instance;
    }

    public synchronized void initSdk(String modelPath, SdkConfig config, Context context) {
        if (hasInit && tengineKitEngine != null) {
            Log.i(SDKConstant.SDK_LOG_TAG, "hasInited before");
            return;
        }
        Log.i(SDKConstant.SDK_LOG_TAG, "initSdk start");
        tengineKitEngine = new TengineKitEngine(modelPath, config);
        Log.i(SDKConstant.SDK_LOG_TAG, "initSdk end");
        hasInit = true;
        if (!config.allowReport) {
            allowReport = false;
            return;
        }
        this.sdkFunction = config.sdkFunction;
        reportInit(context);
    }

    public synchronized void release() {
        if (tengineKitEngine != null) {
            tengineKitEngine.release();
            hasInit = false;
            if (allowReport) {
                reportRelease();
            }
        }
    }

    public Face[] detectFace(ImageConfig imageConfig, FaceConfig faceConfig) {
        if (!hasInit) {
            Log.e(SDKConstant.SDK_LOG_TAG, "please TengineAiSdk.initSDK() before detect");
            return null;
        }
        return tengineKitEngine.detectFace(imageConfig, faceConfig);
    }


    private String packageName = "unKnow";
    private String readDeviceID = "unKnow";

    private void reportInit(Context context) {
        if (context == null) {
            return;
        }
        getID(context);
        new HttpReport(readDeviceID,
                "", "",
                packageName, sdkFunction.functionName(),
                DeviceUtil.getCountryCode(context)
        ).start();
    }


    private void reportRelease() {

    }

    private void getID(Context context) {
        try {
            String readDeviceID = DeviceUtil.readDeviceID(context);
            SharedPreferences mShare = context.getSharedPreferences("uuid", MODE_PRIVATE);
            String string = mShare.getString("uuid", "");
            if (string != null) {
                if (readDeviceID == null && !TextUtils.equals(string, readDeviceID)) {
                    if (TextUtils.isEmpty(readDeviceID) && !TextUtils.isEmpty(string)) {
                        readDeviceID = string;
                        DeviceUtil.saveDeviceID(readDeviceID, context);
                    }
                }
            }
            if (readDeviceID == null) {
                //保存设备id
                readDeviceID = DeviceUtil.getDeviceId(context);
            }
            mShare.edit().putString("uuid", readDeviceID).apply();

            PackageManager packageManager = context.getPackageManager();
            int flag = PackageManager.GET_UNINSTALLED_PACKAGES;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                flag = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }
            String _packageName = "";
            try {
                PackageManager packageManager1 = context.getPackageManager();
                PackageInfo packageInfo = packageManager1.getPackageInfo(context.getPackageName(), 0);
                _packageName = packageInfo.packageName;

            } catch (Exception e) {

                e.printStackTrace();
            }

            this.readDeviceID = readDeviceID;
            this.packageName = _packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
