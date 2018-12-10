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

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

/* $if umplatform == 2  $
    import com.ustadmobile.test.port.j2me.TestCase;
 $else$ */
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
public class TestCacheAndStorageDir extends ActivityInstrumentationTestCase2<UstadMobileActivity> {
 $else$ */
public abstract class TestCacheAndStorageDir extends TestCase{
/* $endif$ */

    public TestCacheAndStorageDir() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        /* $if umplatform == 1  $
        android.app.Activity activity = getActivity();
        $endif  */
    }
    
    public void testCacheDir()  {
//        Object context = UMContextGetter.getContext(this);
        Object context = PlatformTestUtil.getTargetContext();
        TestUtils utils = new TestUtils();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.init(context);
        String cacheDir = impl.getCacheDir(CatalogPresenter.SHARED_RESOURCE,
                context);
        impl.l(UMLog.DEBUG, 595, cacheDir);
        assertTrue("Can make files in cache dir", 
                UMIOUtils.canWriteChildFile(cacheDir));
        impl.setActiveUser(utils.getTestProperty(TestUtils.PROP_TESTUSER), context);
        cacheDir = impl.getCacheDir(CatalogPresenter.USER_RESOURCE, context);
        impl.l(UMLog.DEBUG, 597, cacheDir);
        assertTrue("User cache dir is created",
                UMIOUtils.canWriteChildFile(cacheDir));
        impl.l(UMLog.DEBUG, 593, cacheDir);
        UMStorageDir[] storageDirs = impl.getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE | CatalogPresenter.USER_RESOURCE, context);
        impl.l(UMLog.DEBUG, 599, cacheDir);
        assertTrue("found available storage dirs: ", storageDirs.length >= 2);
    }
    
    public void runTest() {
        testCacheDir();
    }
    
}
