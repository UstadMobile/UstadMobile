package com.toughra.ustadmobile;

import android.app.Activity;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;

import com.ustadmobile.impl.UMProgressEvent;
import com.ustadmobile.impl.UMProgressListener;
import com.ustadmobile.impl.UMTransferJob;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.impl.UstadMobileSystemImplAndroid;

import java.io.File;
import java.util.Hashtable;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.toughra.ustadmobile.UstadMobileActivityTest \
 * com.toughra.ustadmobile.tests/android.test.InstrumentationTestRunner
 */
public class UstadMobileActivityTest extends ActivityInstrumentationTestCase2<UstadMobileActivity> implements UMProgressListener {

    private UMProgressEvent lastProgressEvent;

    public UstadMobileActivityTest() {
        super("com.toughra.ustadmobile", UstadMobileActivity.class);
    }

    public void testDownloadImpl() {
        Activity act = getActivity();
        File baseDir = Environment.getExternalStorageDirectory();
        File file3 = new File(baseDir, "wpdownload.zip");
        if(file3.exists()) {
            file3.delete();
        }


        UMTransferJob job = UstadMobileSystemImpl.getInstance().downloadURLToFile("http://www.ustadmobile.com/wordpress-4.2.2.zip",
                file3.getAbsolutePath(), new Hashtable());
        job.addProgresListener(this);
        job.start();


        int timeout = 120000;
        int interval = 1500;
        int timeCount = 0;

        for(timeCount = 0; timeCount < timeout && !job.isFinished(); timeCount+= interval) {
            try {
                Thread.sleep(1500);
            }catch(InterruptedException e) {}
        }

        int totalSize = job.getTotalSize();
        int downloadedSize = job.getBytesDownloadedCount();
        assertTrue("Downloaded size is the same as total size: "
                + totalSize + " : " + downloadedSize,
                totalSize == downloadedSize);

    }

    public void testUserprefs() {
        String currentUsername = "bobtheamazing";
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.setActiveUser(currentUsername);
        assertEquals("Current username is as set", currentUsername, impl.getActiveUser());
        assertTrue("Current username in Shared Preferences", getActivity().getSharedPreferences(
            UstadMobileSystemImplAndroid.APP_PREFERENCES_NAME, 0).contains(
            UstadMobileSystemImplAndroid.KEY_CURRENTUSER));
    }


    @Override
    public void progressUpdated(UMProgressEvent umProgressEvent) {
        lastProgressEvent = umProgressEvent;
    }
}
