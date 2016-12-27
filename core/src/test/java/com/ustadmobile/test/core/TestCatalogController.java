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


import com.ustadmobile.core.util.TestConstants;
import com.ustadmobile.core.util.TestUtils;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
   $endif$ */

/* $if umplatform == 2  $
    import com.ustadmobile.test.port.j2me.TestCase;
 $else$ */
    import junit.framework.TestCase;
/* $endif$ */

/**
 *
 * @author mike
 */

/* $if umplatform == 1  $
public class TestCatalogController extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public class TestCatalogController extends TestCase{
/* $endif */
    
    private String opdsURL;
    
    private String acquireOPDSURL;
    
    public TestCatalogController() {
        /* $if umplatform == 1 $ 
        super("com.toughra.ustadmobile", UstadMobileActivity.class);
        $endif */
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        /* $if umplatform == 1  $
        android.app.Activity activity = getActivity();
         $endif */
        opdsURL = TestUtils.getInstance().getHTTPRoot() + TestConstants.CATALOG_OPDS_ROOT;
        acquireOPDSURL = TestUtils.getInstance().getHTTPRoot() + "acquire-multi.opds";
    }
    
    public void testCatalogController() throws IOException, XmlPullParserException {
        final Object context = UMContextGetter.getContext(this);
        final TestUtils testUtils = new TestUtils();
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 311, null);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.setActiveUser(testUtils.getTestProperty(TestUtils.PROP_TESTUSER), 
            context);
        
        
        final Hashtable loadedVals = new Hashtable();
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    Hashtable ctrlArgs = new Hashtable();
                    ctrlArgs.put(CatalogController.KEY_URL, opdsURL);
                    ctrlArgs.put(CatalogController.KEY_RESMOD, 
                        new Integer(CatalogController.USER_RESOURCE));
                    ctrlArgs.put(CatalogController.KEY_HTTPUSER, 
                        testUtils.getTestProperty(TestUtils.PROP_TESTUSER));
                    ctrlArgs.put(CatalogController.KEY_HTTPPPASS, 
                        testUtils.getTestProperty(TestUtils.PROP_TESTAUTH));
                    ctrlArgs.put(CatalogController.KEY_FLAGS, new Integer(CatalogController.CACHE_ENABLED));
                    
                    CatalogController loadCtrl = CatalogController.makeControllerByArgsTable(
                        ctrlArgs, context);
                    loadedVals.put("controller1", loadCtrl);
                }catch(Exception e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 183, opdsURL, e);
                }
            }
        }).start();
        
        TestUtils.waitForValueInTable("controller1", loadedVals);
        CatalogController controller = (CatalogController)loadedVals.get("controller1");
        
        assertNotNull("Create catalog controller", controller);
        
        
        UstadJSOPDSFeed feedItem = controller.getModel().opdsFeed;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer serializer = impl.newXMLSerializer();
        serializer.setOutput(bout, "UTF-8");
        feedItem.serialize(serializer);
        bout.flush();
        ByteArrayInputStream bin = new ByteArrayInputStream(
            bout.toByteArray());
        bout = null;
        
        XmlPullParser parser = impl.newPullParser();
        parser.setInput(bin, "UTF-8");
        UstadJSOPDSFeed fromXMLItem = UstadJSOPDSFeed.loadFromXML(parser);
        assertEquals("Same id when reparsed", feedItem.id, fromXMLItem.id);
        CatalogController.cacheCatalog(feedItem, CatalogController.USER_RESOURCE, 
                context);
        UstadJSOPDSFeed cachedFeed = 
            CatalogController.getCachedCatalogByID(feedItem.id, 
            CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE,
            context);
        
        assertEquals("Same feed id on cached catalog", feedItem.id, cachedFeed.id);
        
        //test out selection between different links
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    Hashtable ctrlArgs = new Hashtable();
                    ctrlArgs.put(CatalogController.KEY_URL, acquireOPDSURL);
                    ctrlArgs.put(CatalogController.KEY_RESMOD, 
                        new Integer(CatalogController.USER_RESOURCE));
                    ctrlArgs.put(CatalogController.KEY_HTTPUSER, 
                        testUtils.getTestProperty(TestUtils.PROP_TESTUSER));
                    ctrlArgs.put(CatalogController.KEY_HTTPPPASS, 
                        testUtils.getTestProperty(TestUtils.PROP_TESTAUTH));
                    ctrlArgs.put(CatalogController.KEY_FLAGS, new Integer(CatalogController.CACHE_ENABLED));
                    
                    CatalogController loadCtrl = CatalogController.makeControllerByArgsTable(
                        ctrlArgs, context);
                    loadedVals.put("controller2", loadCtrl);
                }catch(Exception e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 183, opdsURL, e);
                }
            }
        }).start();
        
        TestUtils.waitForValueInTable("controller2", loadedVals);
        controller = (CatalogController)loadedVals.get("controller2");
        
        UstadJSOPDSFeed feed = controller.getModel().opdsFeed;
        CatalogController.AcquireRequest request = new CatalogController.AcquireRequest(
            feed.entries, "/some/dir/notused", 
            testUtils.getTestProperty(TestUtils.PROP_TESTUSER), 
            testUtils.getTestProperty(TestUtils.PROP_TESTAUTH), 
            CatalogController.SHARED_RESOURCE, context, controller);
        
        // test filtering of acquisition links
        Vector filteredLinks = CatalogController.filterAcquisitionLinksByProfile(
            controller.getModel().opdsFeed.entries[0].getAcquisitionLinks(),null);
        String[] filteredLink = (String[])filteredLinks.elementAt(1);
        assertEquals("After filtering two links are remaining", 2, 
            filteredLinks.size());
        assertEquals("After filtering null type only plain link remains",
            "application/epub+zip",filteredLink[UstadJSOPDSItem.ATTR_MIMETYPE]);
        
        // tst filtering acquisition links for the micro profile
        filteredLinks = CatalogController.filterAcquisitionLinksByProfile(
            controller.getModel().opdsFeed.entries[0].getAcquisitionLinks(),
            "micro");
        filteredLink = (String[])filteredLinks.elementAt(1);
        assertEquals("After filtering two links are remaining", 2, 
            filteredLinks.size());
        assertEquals("After filtering micro type micro epub link remains",
            "application/epub+zip;x-umprofile=micro",
            filteredLink[UstadJSOPDSItem.ATTR_MIMETYPE]);
    }
    
    public void runTest() throws IOException, XmlPullParserException{
        testCatalogController();
    }
}
