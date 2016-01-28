package com.garlicg.screenrecordct.util;

import android.util.Log;

import com.garlicg.screenrecordct.BuildConfig;

/**
 * Instant log class
 */
public class Cat {

    private static final String TAG_NAME = "Cat";

    private static final String FORMAT = "%s [#%s (%s:%s)]";

    private static String createMessage(String msg) {
        StackTraceElement e = new Throwable().getStackTrace()[2];
        return String.format(FORMAT, msg, e.getMethodName(), e.getFileName(), e.getLineNumber());
    }

    public static void v(String msg) {
        if (BuildConfig.DEBUG) Log.v(TAG_NAME, createMessage(msg));
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG) Log.d(TAG_NAME, createMessage(msg));
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG) Log.i(TAG_NAME, createMessage(msg));
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG) Log.w(TAG_NAME, createMessage(msg));
    }

    public static void e(String msg) {
        if (BuildConfig.DEBUG) Log.e(TAG_NAME, createMessage(msg));
    }

    private static String sTimeStampTag;
    private static long sTmpTime;

    public static void tick(String tag) {
        if (!BuildConfig.DEBUG) return;
        sTimeStampTag = tag;
        sTmpTime = System.currentTimeMillis();
        Log.i("[Tick]" + tag, createMessage("time stamp start"));
    }

    public static void tack(String message) {
        if (!BuildConfig.DEBUG) return;
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - sTmpTime;
        Log.i("[Tack]" + sTimeStampTag, createMessage("[" + Long.toString(diffTime) + "ms]" + message));
        sTmpTime = currentTime;
    }
}
