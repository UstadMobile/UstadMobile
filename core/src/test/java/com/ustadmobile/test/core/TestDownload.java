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
package com.ustadmobile.test.core;


import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import java.io.IOException;
import java.util.Hashtable;

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */
    import junit.framework.TestCase;
/* $endif$ */

/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
$endif$ */


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestDownload extends ActivityInstrumentationTestCase2<UstadMobileActivity> implements UMDownloadCompleteReceiver{
 $else$ */
public abstract class TestDownload extends TestCase implements  UMDownloadCompleteReceiver{
/* $endif$ */
    
    private boolean downloadFinished = false;
    
    private int[] downloadFinishedStatus = null;
    
    private String downloadID;
    
    private String httpRoot = null;
    
    public TestDownload() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }

    
    protected void setUp() throws Exception {
        super.setUp(); 
        httpRoot = TestUtils.getInstance().getHTTPRoot();
    }
        
    public void testDownloadImpl() throws IOException{
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Object context = UMContextGetter.getContext(this);
        
        impl.registerDownloadCompleteReceiver(this, context);
        
        String destFileURI = UMFileUtil.joinPaths(new String[] {
            impl.getSharedContentDir(), "phonepic-smaller.png"});
        
        if(impl.fileExists(destFileURI)) {
            impl.removeFile(destFileURI);
        }
        
        String fileDownloadURL = httpRoot + "phonepic-smaller.png";
        
        int downloadSize = impl.makeRequest(fileDownloadURL, null, null, "HEAD").getContentLength();
        assertTrue("Can get size before starting", downloadSize > 0);
        
        downloadID = impl.queueFileDownload(fileDownloadURL, destFileURI, 
            null, context);
        
        int timeout = 1 * 60 * 1000;// 1 mins
        int interval = 1500;
        int timeCount = 0;
        
        for(timeCount = 0; timeCount < timeout && !downloadFinished; timeCount+= interval) {
            try {
                Thread.sleep(1500);
            }catch(InterruptedException e) {}
        }
        
        assertTrue("Download job reports completion", downloadFinished);
        impl.unregisterDownloadCompleteReceiver(this, context);

        downloadFinished = false;
        
        long downloadedSize = downloadFinishedStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR];
        
        assertTrue("Downloaded size is the same as total size: "
                + downloadedSize + " : " + downloadedSize,
                downloadedSize == downloadedSize);
        
        assertTrue("Downloaded file exists ", impl.fileExists(destFileURI));
        assertEquals("Downloaded file size equals job download size",
                downloadedSize, impl.fileSize(destFileURI));
        
        impl.removeFile(destFileURI);
        
        /*
         Test the same again... but when there is an interrption to the download
        */        
        boolean limitsSet = TestUtils.getInstance().setLimits(4000, 20000);
        assertTrue("Successfully set download limits on server", limitsSet);
        
        timeout = 4 * 60 * 1000;// 4 mins to allow for waiting in between disconnects
        impl.registerDownloadCompleteReceiver(this, context);
        downloadID = impl.queueFileDownload(fileDownloadURL, destFileURI, 
            null, context);
        
        for(timeCount = 0; timeCount < timeout && !downloadFinished; timeCount+= interval) {
            try {
                Thread.sleep(1500);
            }catch(InterruptedException e) {}
        }
        
        assertTrue("Download job reports completion (with interruptions)", 
            downloadFinishedStatus[UstadMobileSystemImpl.IDX_STATUS] == UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL);
        downloadedSize = downloadFinishedStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR];
        assertEquals("Downloaded file size equals job download size (with interruptions)",
                downloadedSize, impl.fileSize(destFileURI));
        
        
        impl.unregisterDownloadCompleteReceiver(this, context);
    }

    protected void tearDown() throws Exception {
        super.tearDown(); 
        TestUtils.getInstance().setLimits(0, 0);
    }

    public void downloadStatusUpdated(UMDownloadCompleteEvent evt) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 500, 
                "TestDownload.downloadStatusReceived: id" + evt.getDownloadID());
        if(evt.getDownloadID() == downloadID) {
            downloadFinished = true;
            downloadFinishedStatus = evt.getStatus();
        }
    }
    
    /* $if umplatform == 2 $ 
    public void runTest() throws IOException {
        this.testDownloadImpl();
    }
    $endif */

    
}
