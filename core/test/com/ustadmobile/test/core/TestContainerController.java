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
    import junit.framework.TestCase;
/* $endif$ */

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import static junit.framework.Assert.assertTrue;
import org.xmlpull.v1.XmlPullParserException;


/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestContainerController extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public class TestContainerController extends TestCase {
/* $endif */

     /**
     * Interval at which to check on if the job has finished
     */
    public static final int CHECKINTERVAL = 1000;
    
    /**
     * Timeout - maximum time allowed for test acquisition of feed to succeed
     */
    public static final int TIMEOUT = 60000;
    
    public TestContainerController() {
        /* $if umplatform == 1 $ 
        super("com.toughra.ustadmobile", UstadMobileActivity.class);
        $endif */
    }
    
    public void testContainerController() throws IOException, XmlPullParserException{
        String httpRoot = TestConstants.TEST_HTTP_ROOT;
        
        String acquireOPDSURL = UMFileUtil.joinPaths(new String[] {
            httpRoot, "acquire.opds"});
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(acquireOPDSURL, 
            CatalogController.SHARED_RESOURCE, null, null, 
            CatalogController.CACHE_ENABLED);
        
        UMTransferJob acquireJob = CatalogController.acquireCatalogEntries(feed.entries, 
            null, null, CatalogController.SHARED_RESOURCE, CatalogController.CACHE_ENABLED);
        int totalSize = acquireJob.getTotalSize();
        
        acquireJob.start();
        int timeRemaining = TIMEOUT;
        while(timeRemaining > 0 && !acquireJob.isFinished()) {
            try {Thread.sleep(CHECKINTERVAL); }
            catch(InterruptedException e) {}
        }
        assertTrue("Job has completed", acquireJob.isFinished());
        
        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(feed.entries[0].id, 
            CatalogController.SHARED_RESOURCE);
        
        String acquiredFileURI = entryInfo.fileURI;
        
        UstadJSOPDSEntry entry = feed.entries[0];
        
        String openPath = impl.openContainer(entry, acquiredFileURI, 
            entryInfo.mimeType);
        assertNotNull("Got an open path from the system", openPath);
        
        ContainerController controller = ContainerController.makeFromEntry(entry, 
            openPath, entryInfo.fileURI, entryInfo.mimeType);
        UstadOCF ocf = controller.getOCF();
        assertNotNull("Controller can fetch OCF once open", ocf);
        
        UstadJSOPF opf = controller.getOPF(0);
        assertNotNull("Can load package OPF", opf);
        assertTrue("Package has spine with entries", opf.spine.length > 0);
    }
    
}
