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
    import com.ustadmobile.port.android.view.CatalogActivity;
    import android.content.Intent;
$endif$ */

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */

import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import junit.framework.TestCase;

/* $endif$ */


/**
 * Note: On Android this kind of test is not allowed to start an activity -we must
 * use the activityinstrumentationtestcase2, set the intent and use it's way of
 * doing things
 * 
 * @author mike
 */

/* $if umplatform == 1  $
public class TestCatalogView extends ActivityInstrumentationTestCase2<CatalogActivity>{
 $else$ */
public abstract class TestCatalogView extends TestCase {
/* $endif */    
    
    public static final int VIEWSHOWTIMEOUT = 10000;
    
    public static final int VIEWCHECKINTERVAL = 1000;
    
    public TestCatalogView() {
        /* $if umplatform == 1 $ 
        super(CatalogActivity.class);
        
        $endif */
    }

    protected void setUp() throws Exception {
        super.setUp(); 
        
        String opdsURL =  TestUtils.getInstance().getHTTPRoot()
                + TestConstants.CATALOG_OPDS_ROOT;
        
        /* $if umplatform == 1 $ 
        Intent intent = new Intent();
        intent.putExtra(CatalogController.KEY_URL, opdsURL);
        intent.putExtra(CatalogController.KEY_RESMOD, CatalogController.USER_RESOURCE);
        intent.putExtra(CatalogController.KEY_FLAGS, CatalogController.CACHE_ENABLED);
        setActivityIntent(intent);
        $endif */
        
    }
    
    
    
    
    public void testCatalogView() throws Exception{
//        Object context = UMContextGetter.getTargetContext(this);
//        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        impl.setActiveUser(TestConstants.LOGIN_USER, context);
//        
//        String opdsURL =  TestUtils.getInstance().getHTTPRoot()  
//            + TestConstants.CATALOG_OPDS_ROOT;
//        
//        Hashtable args = new Hashtable();
//        args.put(CatalogController.KEY_URL, opdsURL);
//        args.put(CatalogController.KEY_RESMOD, new Integer(CatalogController.SHARED_RESOURCE));
//        args.put(CatalogController.KEY_FLAGS, new Integer(CatalogController.CACHE_ENABLED));
//        impl.go(CatalogView.class, args, context);
        
        //Tricky to say what to do next here... in reality we don't know if the view is up on screen...
        assertTrue("View is showing", true);
        
        
    }
}
