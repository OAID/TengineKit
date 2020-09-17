package com.faceDemo.utils;

import android.util.Log;

public class MyLogger {
    private static boolean isDebug = true;

    public static void logDebug(String tag ,Object content)
    {
        if(isDebug)
        {
            Log.d(tag,"-------------------------  "+content);
        }
    }

    public static void logInfo(String tag ,String content)
    {
        if(isDebug)
        {
            Log.i(tag,"-------------------------  "+content);
        }
    }

    public static void logError(String tag ,String content)
    {
        if(isDebug)
        {
            Log.e(tag,"-------------------------  "+content);
        }
    }
}
