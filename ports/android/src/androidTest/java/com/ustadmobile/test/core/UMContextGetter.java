package com.ustadmobile.test.core;

import android.test.ActivityInstrumentationTestCase2;

import junit.framework.TestCase;

/**
 * Created by mike on 9/29/15.
 */
public class UMContextGetter {

    public static Object getContext(TestCase tc) {
        if(tc instanceof ActivityInstrumentationTestCase2) {
            return ((ActivityInstrumentationTestCase2)tc).getActivity();
        }else {
            throw new IllegalArgumentException("UmContextGetter for android tests: must have ActivityInstrumentationTestCase2 argument");
        }
    }

}
