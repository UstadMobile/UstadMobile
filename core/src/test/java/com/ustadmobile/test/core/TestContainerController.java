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
    import j2meunit.framework.TestCase;
 $else$ */

import com.ustadmobile.core.util.TestUtils;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Hashtable;

/* $endif$ */


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestContainerController extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public abstract class TestContainerController extends TestCase  {
/* $endif */

     /**
     * Interval at which to check on if the job has finished
     */
    public static final int CHECKINTERVAL = 1000;
    
    /**
     * Timeout - maximum time allowed for test acquisition of feed to succeed
     */
    public static final int TIMEOUT = 60000;
    
    private String httpRoot;
    
    private final Hashtable loadedVals;
    
    public TestContainerController() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
        loadedVals = new Hashtable();
    }

    protected void setUp() throws Exception {
        super.setUp(); 
        httpRoot = TestUtils.getInstance().getHTTPRoot();
    }
    
    
    
    public void testContainerController() throws IOException, XmlPullParserException{
        
        //TODO: Update me for the new ViewFirst model
//        final Hashtable loadedVals = new Hashtable();
//        final Object context = UMContextGetter.getTargetContext(this);
//        final String acquireOPDSURL = UMFileUtil.joinPaths(new String[] {
//            httpRoot, "acquire.opds"});
//        
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    CatalogController loadCtrl = CatalogController.makeControllerByURL(
//                        acquireOPDSURL, CatalogController.USER_RESOURCE, 
//                        TestConstants.LOGIN_USER, TestConstants.LOGIN_PASS, 
//                        CatalogController.CACHE_ENABLED, context);
//                    loadedVals.put("catalogctrl", loadCtrl);
//                }catch(Exception e) {
//                    UstadMobileSystemImpl.l(UMLog.ERROR, 183, acquireOPDSURL, e);
//                }
//            }
//        }).start();
//        
//        TestUtils.waitForValueInTable("catalogctrl", loadedVals);
//        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        
//        
//        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(acquireOPDSURL, 
//            CatalogController.SHARED_RESOURCE, null, null, 
//            CatalogController.CACHE_ENABLED);
//        
//        //make sure if the entry is around... we remove it...
//        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        if(entryInfo != null && entryInfo.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
//            CatalogController.removeEntry(feed.entries[0].id, CatalogController.SHARED_RESOURCE);
//        }
//        
//        entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        boolean entryPresent = entryInfo == null || entryInfo.acquisitionStatus != CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
//        assertTrue("Entry not acquired at start of test", entryPresent);
//        
//        UMStorageDir[] dirs = impl.getStorageDirs(CatalogController.SHARED_RESOURCE);
//        CatalogController.AcquireRequest request = new CatalogController.AcquireRequest(
//            feed.entries, dirs[0].getDirURI(), null, null, CatalogController.SHARED_RESOURCE);
//        
//        UMTransferJob acquireJob = CatalogController.acquireCatalogEntries(request);
//        int totalSize = acquireJob.getTotalSize();
//        
//        acquireJob.start();
//        int timeRemaining = TIMEOUT;
//        while(timeRemaining > 0 && !acquireJob.isFinished()) {
//            try {Thread.sleep(CHECKINTERVAL); }
//            catch(InterruptedException e) {}
//        }
//        assertTrue("Job has completed", acquireJob.isFinished());
//        impl.getLogger().l(UMLog.INFO, 800, "sleeping");
//	
//        entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
//            CatalogController.SHARED_RESOURCE);
//        
//        String acquiredFileURI = entryInfo.fileURI;
//        
//        UstadJSOPDSEntry entry = feed.entries[0];
//        
//        String openPath = impl.openContainer(acquiredFileURI, 
//            entryInfo.mimeType);
//        assertNotNull("Got an open path from the system", openPath);
//        
//        ContainerController controller = ContainerController.makeFromEntry(entry, 
//            openPath, entryInfo.fileURI, entryInfo.mimeType);
//        UstadOCF ocf = controller.getOCF();
//        assertNotNull("Controller can fetch OCF once open", ocf);
//        
//        UstadJSOPF opf = controller.getOPF(0);
//        assertNotNull("Can load package OPF", opf);
//        assertTrue("Package has spine with entries", opf.spine.length > 0);
//        
//        
//        //delete it now we are done
//        impl.closeContainer(openPath);
//        CatalogController.removeEntry(entry.id, CatalogController.SHARED_RESOURCE);
//
//	assertTrue(true);
        
    }
    
    public void runTests() throws IOException, XmlPullParserException{
	this.testContainerController();
    }

    
}
