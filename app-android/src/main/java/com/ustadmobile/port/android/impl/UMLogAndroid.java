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
        switch(level) {
            case UMLog.DEBUG:
                Log.d(LOGTAG,logMessage);
                break;
            case UMLog.INFO:
                Log.i(LOGTAG, logMessage);
                break;
            case UMLog.CRITICAL:
                Log.wtf(LOGTAG, logMessage);
                break;
            case UMLog.WARN:
                Log.w(LOGTAG, logMessage);
                break;
            case UMLog.VERBOSE:
                Log.v(LOGTAG, logMessage);
                break;
            case UMLog.ERROR:
                Log.e(LOGTAG, logMessage);
                break;

        }
    }

    @Override
    public void l(int level, int code, String message, Exception exception) {
        String logMessage = code + " : " + message;
        switch(level) {
            case UMLog.DEBUG:
                Log.d(LOGTAG,logMessage, exception);
                break;
            case UMLog.INFO:
                Log.i(LOGTAG, logMessage, exception);
                break;
            case UMLog.CRITICAL:
                Log.wtf(LOGTAG, logMessage, exception);
                break;
            case UMLog.WARN:
                Log.w(LOGTAG, logMessage, exception);
                break;
            case UMLog.VERBOSE:
                Log.v(LOGTAG, logMessage, exception);
                break;
            case UMLog.ERROR:
                Log.e(LOGTAG, logMessage, exception);
                break;

        }
    }
}
