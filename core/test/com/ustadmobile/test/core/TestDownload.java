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


import com.ustadmobile.core.impl.UMProgressEvent;
import com.ustadmobile.core.impl.UMProgressListener;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
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
        import com.toughra.ustadmobile.UMActivityInstrumentationTestCase2;
$endif$ */


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestDownload extends UMActivityInstrumentationTestCase2<UstadMobileActivity> implements UMProgressListener{
 $else$ */
public class TestDownload extends TestCase implements UMProgressListener {
/* $endif$ */
    
    public TestDownload() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    private UMProgressEvent lastProgressEvent = null;
    
    public void testDownloadImpl() throws IOException{
//        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        String destFileURI = UMFileUtil.joinPaths(new String[] {
//            impl.getSharedContentDir(), "phonepic-smaller.png"});
//        
//        if(impl.fileExists(destFileURI)) {
//            impl.removeFile(destFileURI);
//        }
//        
//        String fileDownloadURL = TestUtils.getInstance().getHTTPRoot() + "phonepic-smaller.png";
//        UMTransferJob job = UstadMobileSystemImpl.getInstance().downloadURLToFile(fileDownloadURL,
//                destFileURI, new Hashtable());
//        
//        int jobSize = job.getTotalSize();
//        
//        assertTrue("Can get size before starting", jobSize > 0);
//        
//        job.addProgressListener(this);
//        job.start();
//
//
//        int timeout = 1 * 60 * 1000;// 1 mins
//        int interval = 1500;
//        int timeCount = 0;
//
//        for(timeCount = 0; timeCount < timeout && !job.isFinished(); timeCount+= interval) {
//            try {
//                Thread.sleep(1500);
//            }catch(InterruptedException e) {}
//        }
//        
//        assertTrue("Download job reports completion", job.isFinished());
//
//        int finalSize = job.getTotalSize();
//        long downloadedSize = job.getBytesDownloadedCount();
//        assertTrue("Downloaded size is the same as total size: "
//                + finalSize + " : " + downloadedSize,
//                finalSize == downloadedSize);
//        
//        assertTrue("Downloaded file exists ", impl.fileExists(destFileURI));
//        assertEquals("Downloaded file size equals job download size",
//                finalSize, impl.fileSize(destFileURI));
//        
//        impl.removeFile(destFileURI);
//        
//        /*
//         Test the same again... but when there is an interrption to the download
//        */
//        boolean limitsSet = TestUtils.getInstance().setLimits(4000, 20000);
//        assertTrue("Successfully set download limits on server", limitsSet);
//        
//        timeout = 4 * 60 * 1000;// 4 mins to allow for waiting in between disconnects
//        job = UstadMobileSystemImpl.getInstance().downloadURLToFile(fileDownloadURL,
//                destFileURI, new Hashtable());
//        job.start();
//        for(timeCount = 0; timeCount < timeout && !job.isFinished(); timeCount+= interval) {
//            try {
//                Thread.sleep(1500);
//            }catch(InterruptedException e) {}
//        }
//        
//        assertTrue("Download job reports completion (with interruptions)", 
//            job.isFinished());
//        assertEquals("Downloaded file size equals job download size (with interruptions)",
//                finalSize, impl.fileSize(destFileURI));
    }

    protected void tearDown() throws Exception {
        super.tearDown(); 
        TestUtils.getInstance().setLimits(0, 0);
    }
    
    
    
    public void runTest() throws IOException {
        this.testDownloadImpl();
    }

    public void progressUpdated(UMProgressEvent evt) {
        lastProgressEvent = evt;
    }
    
}
