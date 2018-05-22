package com.ustadmobile.test.sharedse.impl;

import com.ustadmobile.core.impl.UMLog;

/**
 * Created by mike on 4/29/17.
 */

public class UMTestLogger extends UMLog {

    @Override
    public void l(int level, int code, String message) {
        System.out.println(level + ":" + code + " : " + message);
    }

    @Override
    public void l(int level, int code, String message, Exception exception) {
        System.out.println(level + ":" + code + " : " + message);
        exception.printStackTrace();
    }
}
