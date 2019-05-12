package com.ustadmobile.port.android.impl;

import android.util.Log;

import com.ustadmobile.core.impl.UMLogger;

/**
 * Created by mike on 8/13/15.
 */
public class UMLogAndroid  extends UMLogger {

    public static String LOGTAG = "UMLogAndroid";


    @Override
    public void l(int level, int code, String message) {
        String logMessage = code + " : " + message;
        if (level == UMLogger.DEBUG) {
            Log.d(LOGTAG, logMessage);

        } else if (level == UMLogger.INFO) {
            Log.i(LOGTAG, logMessage);

        } else if (level == UMLogger.CRITICAL) {
            Log.wtf(LOGTAG, logMessage);

        } else if (level == UMLogger.WARN) {
            Log.w(LOGTAG, logMessage);

        } else if (level == UMLogger.VERBOSE) {
            Log.v(LOGTAG, logMessage);

        } else if (level == UMLogger.ERROR) {
            Log.e(LOGTAG, logMessage);

        }
    }

    @Override
    public void l(int level, int code, String message, Exception exception) {
        String logMessage = code + " : " + message;
        if (level == UMLogger.DEBUG) {
            Log.d(LOGTAG, logMessage, exception);

        } else if (level == UMLogger.INFO) {
            Log.i(LOGTAG, logMessage, exception);

        } else if (level == UMLogger.CRITICAL) {
            Log.wtf(LOGTAG, logMessage, exception);

        } else if (level == UMLogger.WARN) {
            Log.w(LOGTAG, logMessage, exception);

        } else if (level == UMLogger.VERBOSE) {
            Log.v(LOGTAG, logMessage, exception);

        } else if (level == UMLogger.ERROR) {
            Log.e(LOGTAG, logMessage, exception);

        }
    }
}
