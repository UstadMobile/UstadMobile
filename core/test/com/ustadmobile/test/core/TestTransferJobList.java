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

/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
   $endif$ */

/* $if umplatform == 2  $
    import org.j2meunit.framework.TestCase;
 $else$ */
import com.ustadmobile.core.impl.UMProgressEvent;
import com.ustadmobile.core.impl.UMProgressListener;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UMTransferJobList;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.util.Hashtable;
    import junit.framework.TestCase;
/* $endif$ */

/**
 *
 * @author mike
 */

/* $if umplatform == 1  $
public class TestTransferJobList extends ActivityInstrumentationTestCase2<UstadMobileActivity> implements UMProgressListener{
 $else$ */
public class TestTransferJobList extends TestCase implements UMProgressListener{
/* $endif */
    
    public static int TIMEOUT = 60000;
    
    public static int INTERVAL = 2000;
    
    boolean completed = false;
    
    public TestTransferJobList() {
        /* $if umplatform == 1 $ 
        super("com.toughra.ustadmobile", UstadMobileActivity.class);
        $endif */
        completed = false;
    }
    
    public void testTransferJobList() throws Exception {
        TestRunnable beforeStartJob = new TestRunnable();
        TestRunnable afterFinishJob = new TestRunnable();
        
        String url1 = TestConstants.TEST_HTTP_ROOT + "phonepic-large.png";
        String url2 = TestConstants.TEST_HTTP_ROOT + "root.opds";
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        String filePath1 = impl.getSharedContentDir() +"/testlist-phonepic-large.png";
        String filePath2 = impl.getSharedContentDir()+ "/testlist-root.opds";
        
        //delete just in case there are leftovers laying around...
        impl.removeFile(filePath1);
        impl.removeFile(filePath2);
        
        Hashtable requestHeaders = new Hashtable();
        UMTransferJob job1 = impl.downloadURLToFile(url1, filePath1, requestHeaders);
        UMTransferJob job2 = impl.downloadURLToFile(url2, filePath2, requestHeaders);
        
        UMTransferJobList jobList = new UMTransferJobList(
            new UMTransferJob[]{job1, job2});
        jobList.setRunBeforeStartJob(beforeStartJob);
        jobList.setRunAfterFinishJob(afterFinishJob);
        
        int listSize = jobList.getTotalSize();
        jobList.addProgresListener(this);
        assertTrue("Can get positive total size for download list", listSize > 0);
        jobList.start();
        
        assertTrue("Run before start job runnable", beforeStartJob.hasRun());
        int timeRemaining = TIMEOUT;
        while(timeRemaining > 0 && !jobList.isFinished()) {
            try { Thread.sleep(INTERVAL); }
            catch(InterruptedException e) {}
            timeRemaining -= INTERVAL;
        }
            
        assertTrue("Completed download of list items", this.completed);
        assertTrue("Run after finish job has run", afterFinishJob.hasRun());
        impl.removeFile(filePath1);
        impl.removeFile(filePath2);
    }

    
    public void progressUpdated(UMProgressEvent evt) {
        if(evt.getEvtType() == UMProgressEvent.TYPE_COMPLETE) {
            completed = true;
        }
    }
    
    private class TestRunnable implements Runnable {
        private boolean ran;
        
        public void run() {
            ran = true;
        }
        
        public boolean hasRun() {
            return ran;
        }
    }

}
