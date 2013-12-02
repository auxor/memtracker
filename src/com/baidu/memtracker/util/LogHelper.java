package com.baidu.memtracker.util;

import android.util.Log;

public final class LogHelper {

    /**
     * Primary log tag for games output.
     */
    private static final String LOG_TAG = "BaiduNotif";

    /**
     * Whether the logs are enabled in release builds or not.
     */
    private static final boolean ENABLE_LOGS_IN_RELEASE = false;

    public static boolean canLog(int level) {
        return ENABLE_LOGS_IN_RELEASE && Log.isLoggable(LOG_TAG, level);
    }

    public static void d(String tag, String message) {
        if (canLog(Log.DEBUG)) {
            LogHelper.d(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (canLog(Log.VERBOSE)) {
            LogHelper.v(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (canLog(Log.INFO)) {
            LogHelper.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable thr) {
        if (canLog(Log.INFO)) {
            LogHelper.i(tag, message, thr);
        }
    }

    public static void w(String tag, String message) {
        if (canLog(Log.WARN)) {
            LogHelper.w(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable thr) {
        if (canLog(Log.WARN)) {
            LogHelper.w(tag, message, thr);
        }
    }

    public static void e(String tag, String message) {
        if (canLog(Log.ERROR)) {
            LogHelper.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable thr) {
        if (canLog(Log.ERROR)) {
            LogHelper.e(tag, message, thr);
        }
    }
}
