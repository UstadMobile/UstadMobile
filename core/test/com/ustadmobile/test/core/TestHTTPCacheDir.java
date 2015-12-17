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

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */
    import junit.framework.TestCase;
/* $endif$ */

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.UMFileUtil;


/**
 *
 * @author mike
 */
public class TestHTTPCacheDir extends TestCase {
    
    private String httpRoot;
    
    protected void setUp() throws Exception {
        httpRoot = TestUtils.getInstance().getHTTPRoot();
    }
    
    public void testHTTPCacheDir() throws Exception {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        //context is not needed when we are asking for the shaerd cache dir
        String cacheDirName = impl.getCacheDir(CatalogController.SHARED_RESOURCE, 
            new Object());
        HTTPCacheDir cacheDir = new HTTPCacheDir(cacheDirName);
        
        String httpURL = UMFileUtil.joinPaths(new String[] {httpRoot, 
            "phonepic-smaller.png"});
        
        //make sure that we can fetch a file
        String cachedFile = cacheDir.fetch(httpURL);
        assertTrue("Cached file downloaded", impl.fileExists(cachedFile));
        boolean savedOK = cacheDir.saveIndex();
        assertTrue("Cache dir can save OK", savedOK);
        
        cacheDir = new HTTPCacheDir(cacheDirName);
        String fileURI = cacheDir.getCacheFileURIByURL(httpURL);
        assertNotNull("HTTP URL is known on creating new cache dir object", 
            fileURI);
        
        long date1 = HTTPCacheDir.parseHTTPDate("Sun, 06 Nov 1994 08:49:37 GMT");
        long date2 = HTTPCacheDir.parseHTTPDate("Sunday, 06-Nov-94 08:49:37 GMT");
        long date3 = HTTPCacheDir.parseHTTPDate("Sun Nov  6 08:49:37 1994 ");
        
        //The time in milliseconds between teh date given and the date calculated
        // accurate to within one second
        assertTrue("Can parse date 1", Math.abs(784111777137L -date1) <= 1000);
        assertTrue("Can parse date 2", Math.abs(784111777137L -date2) <= 1000);
        assertTrue("Can parse date 3", Math.abs(784111777137L -date3) <= 1000);
    }
    
    protected void runTest() throws Throwable {
        testHTTPCacheDir();
    }
    
}
