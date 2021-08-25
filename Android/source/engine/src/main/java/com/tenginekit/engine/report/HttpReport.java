package com.tenginekit.engine.report;

import android.net.Uri;
import android.util.Log;

import com.tenginekit.engine.SDKConstant;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class HttpReport extends Thread {
    private final String url = "";

    private String uid;
    private String event;
    private String eventContent;
    private String appInfo;
    private String function;
    private String countryCode;

    public HttpReport(String uid, String event, String eventContent,
                      String appInfo, String function, String countryCode) {
        this.uid = uid;
        this.event = event;
        this.eventContent = eventContent;
        this.appInfo = appInfo;
        this.function = function;
        this.countryCode = countryCode;
    }

    @Override
    public void run() {
        Random r = new Random();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("cloud.openailab.com:8888")
                .appendPath("device")
                .appendPath("mobile")
                .appendPath("report")
                .appendQueryParameter("r", r.nextInt(10000) + "")
                .appendQueryParameter("event", event)
                .appendQueryParameter("eventContent", eventContent)
                .appendQueryParameter("uid", uid)
                .appendQueryParameter("appInfo", appInfo);


//        .appendQueryParameter("function", function)
//                .appendQueryParameter("countryCode", countryCode)
        String requestUri = builder.build().toString().replace("%3A",":");
        Log.i(SDKConstant.SDK_LOG_TAG, requestUri);
        try {
            URL url = new URL(requestUri);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            int resCode = conn.getResponseCode();
            if (resCode == 200) {
                InputStream is = conn.getInputStream();
                byte[] b = new byte[1024];
                int len = 0;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while ((len = is.read(b)) != -1) {
                    bos.write(b, 0, len);
                }
                String text = new String(bos.toByteArray(), "utf-8");
                Log.i(SDKConstant.SDK_LOG_TAG, "report success" + text);
            } else {
                Log.i(SDKConstant.SDK_LOG_TAG, "report fail" + resCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.i(SDKConstant.SDK_LOG_TAG,"error");
            e.printStackTrace();
        }
    }
}
