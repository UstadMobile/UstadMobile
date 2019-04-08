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
            case UMLog.Companion.getDEBUG():
                Log.d(LOGTAG,logMessage);
                break;
            case UMLog.Companion.getINFO():
                Log.i(LOGTAG, logMessage);
                break;
            case UMLog.Companion.getCRITICAL():
                Log.wtf(LOGTAG, logMessage);
                break;
            case UMLog.Companion.getWARN():
                Log.w(LOGTAG, logMessage);
                break;
            case UMLog.Companion.getVERBOSE():
                Log.v(LOGTAG, logMessage);
                break;
            case UMLog.Companion.getERROR():
                Log.e(LOGTAG, logMessage);
                break;

        }
    }

    @Override
    public void l(int level, int code, String message, Exception exception) {
        String logMessage = code + " : " + message;
        switch(level) {
            case UMLog.Companion.getDEBUG():
                Log.d(LOGTAG,logMessage, exception);
                break;
            case UMLog.Companion.getINFO():
                Log.i(LOGTAG, logMessage, exception);
                break;
            case UMLog.Companion.getCRITICAL():
                Log.wtf(LOGTAG, logMessage, exception);
                break;
            case UMLog.Companion.getWARN():
                Log.w(LOGTAG, logMessage, exception);
                break;
            case UMLog.Companion.getVERBOSE():
                Log.v(LOGTAG, logMessage, exception);
                break;
            case UMLog.Companion.getERROR():
                Log.e(LOGTAG, logMessage, exception);
                break;

        }
    }
}
