/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UMTransferJobList;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Hashtable;

public class SplashScreenActivity extends ActionBarActivity {

    public static String TEST_HTTP_ROOT = "http://192.168.0.102:5062/";

    public static final int TIMEOUT = 30000;

    public static final int INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        ((UstadMobileSystemImplAndroid)impl).setCurrentContext(this);
        //testDownloadImpl();
        try {
            //testTransferJobList();
        }catch(Exception e) {
            e.printStackTrace();
        }


        impl.startUI();
    }

    public void testTransferJobList() throws Exception {
        String url1 = TEST_HTTP_ROOT + "phonepic-large.png";
        String url2 = TEST_HTTP_ROOT + "root.opds";
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String filePath1 = impl.getSharedContentDir() +"/testlists-phonepic-large.png";
        String filePath2 = impl.getSharedContentDir()+ "/testlists-root.opds";



        Hashtable requestHeaders = new Hashtable();
        UMTransferJob job1 = impl.downloadURLToFile(url1, filePath1, requestHeaders);
        UMTransferJob job2 = impl.downloadURLToFile(url2, filePath2, requestHeaders);

        UMTransferJobList jobList = new UMTransferJobList(
                new UMTransferJob[]{job1, job2});
        //Crashes on 2.3 here
        int listSize = jobList.getTotalSize();

        jobList.start();
        int timeRemaining = TIMEOUT;
        while(timeRemaining > 0 && !jobList.isFinished()) {
            try { Thread.sleep(INTERVAL); }
            catch(InterruptedException e) {}
            timeRemaining -= INTERVAL;
        }

        boolean isCompleted = jobList.isFinished();

        impl.removeFile(filePath1);
        impl.removeFile(filePath2);
    }


    public void testDownloadImpl() {
        File baseDir = Environment.getExternalStorageDirectory();
        File file3 = new File(baseDir, "sscreen-phonepic-large.png");
        if(file3.exists()) {
            file3.delete();
        }

        String fileDownloadURL = "http://192.168.0.102:5062/" + "phonepic-large.png";
        UMTransferJob job = UstadMobileSystemImpl.getInstance().downloadURLToFile(fileDownloadURL,
                file3.getAbsolutePath(), new Hashtable());
        job.start();


        int timeout = 30000;
        int interval = 1500;
        int timeCount = 0;

        for(timeCount = 0; timeCount < timeout && !job.isFinished(); timeCount+= interval) {
            try {
                Thread.sleep(1500);
            }catch(InterruptedException e) {}
        }

        int totalSize = job.getTotalSize();
        int downloadedSize = job.getBytesDownloadedCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
