package com.ustadmobile.port.android.impl;

import android.util.Log;

import com.ustadmobile.core.impl.UMLog;

/**
 * Created by mike on 8/13/15.
 */
public class UMLogAndroid  extends UMLog{

    public static String LOGTAG = "UMLogAndroid";


    @Override
    public void l(int level, int code, String message) {
        String logMessage = code + " : " + message;
        if (level == UMLog.DEBUG) {
            Log.d(LOGTAG, logMessage);

        } else if (level == UMLog.INFO) {
            Log.i(LOGTAG, logMessage);

        } else if (level == UMLog.CRITICAL) {
            Log.wtf(LOGTAG, logMessage);

        } else if (level == UMLog.WARN) {
            Log.w(LOGTAG, logMessage);

        } else if (level == UMLog.VERBOSE) {
            Log.v(LOGTAG, logMessage);

        } else if (level == UMLog.ERROR) {
            Log.e(LOGTAG, logMessage);

        }
    }

    @Override
    public void l(int level, int code, String message, Exception exception) {
        String logMessage = code + " : " + message;
        if (level == UMLog.DEBUG) {
            Log.d(LOGTAG, logMessage, exception);

        } else if (level == UMLog.INFO) {
            Log.i(LOGTAG, logMessage, exception);

        } else if (level == UMLog.CRITICAL) {
            Log.wtf(LOGTAG, logMessage, exception);

        } else if (level == UMLog.WARN) {
            Log.w(LOGTAG, logMessage, exception);

        } else if (level == UMLog.VERBOSE) {
            Log.v(LOGTAG, logMessage, exception);

        } else if (level == UMLog.ERROR) {
            Log.e(LOGTAG, logMessage, exception);

        }
    }
}
