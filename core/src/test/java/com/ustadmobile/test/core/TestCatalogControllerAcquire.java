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


import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import junit.framework.TestCase;

/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
   $endif$ */
/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
    import com.ustadmobile.core.impl.UMLog;
 $else$ */
/* $endif$ */

/**
 *
 * @author mike
 */

/* $if umplatform == 1  $
public class TestCatalogControllerAcquire extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public abstract class TestCatalogControllerAcquire extends TestCase{
/* $endif */
    
    /**
     * Interval at which to check on if the job has finished
     */
    public static final int CHECKINTERVAL = 1000;
    
    /**
     * Timeout - maximum time allowed for test acquisition of feed to succeed
     */
    public static final int TIMEOUT = 60000;
    
    public TestCatalogControllerAcquire() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        /* $if umplatform == 1  $
        android.app.Activity activity = getActivity();
         $endif */
    }
    
    public void testCatalogControllerAcquire() throws Exception{
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        //TODO: Update this for the new download manager implementation
//        String acquireOPDSURL = UMFileUtil.joinPaths(new String[] {
//             TestUtils.getInstance().getHTTPRoot(), "acquire.opds"});
//        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        
//        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(acquireOPDSURL, 
//            CatalogController.SHARED_RESOURCE, null, null, 
//            CatalogController.CACHE_ENABLED);
//        
//        UMStorageDir[] dirs = impl.getStorageDirs(CatalogController.SHARED_RESOURCE);
//        
//        CatalogController.AcquireRequest request = new CatalogController.AcquireRequest(
//            feed.entries, dirs[0].getDirURI(), null, null, CatalogController.SHARED_RESOURCE);
//        
//        UMTransferJob acquireJob = CatalogController.acquireCatalogEntries(request);
//        int totalSize = acquireJob.getTotalSize();
//        assertTrue("Can count transfer size", totalSize > 0);
//        
//        acquireJob.start();
//        int timeRemaining = TIMEOUT;
//        while(timeRemaining > 0 && !acquireJob.isFinished()) {
//            try {Thread.sleep(CHECKINTERVAL); }
//            catch(InterruptedException e) {}
//        }
//        assertTrue("Job has completed", acquireJob.isFinished());
        
	/* $if umplatform == 2 $
	    impl.getLogger().l(UMLog.INFO, 800, "sleeping");
	    //we sleep because once the opds is downloaded, it will create the opds cache which will conflict with deleting the epub itself. 
	    //Ideally we should check if the caching is also finished in addition to the download job being finished.
	    Thread.sleep(8000);
        $endif */

//        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        
//        String acquiredFileURI = entryInfo.fileURI;
//        
//        assertNotNull("Can obtain catalogEntryInfo for first downloaded item", 
//            entryInfo);
//        assertEquals("Status of entry is now acquired", 
//            CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED, 
//            entryInfo.acquisitionStatus);
//        assertTrue("Destination file container exists", 
//                impl.fileExists(entryInfo.fileURI));
//                
//        CatalogController.removeEntry(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        assertNull("Catalog entry no longer available after deleted",
//            entryInfo);
//        assertTrue("Entry file no longer present after delete",
//                !impl.fileExists(acquiredFileURI));
    }

    public void runTest() throws Exception{
        this.testCatalogControllerAcquire();
    }
    
}
