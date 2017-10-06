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
    /* $endif$ */

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;


import org.json.JSONArray;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Hashtable;

import fi.iki.elonen.router.RouterNanoHTTPD;


/**
 *
 * @author mike
 */
public class TestHTTPCacheDir  {
    

    private static RouterNanoHTTPD resourcesHttpd;

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    @Test
    public void testHTTPCacheDir() throws Exception {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        //context is not needed when we are asking for the shaerd cache dir
        String cacheDirName = impl.getCacheDir(CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        HTTPCacheDir cacheDir = new HTTPCacheDir(cacheDirName, null);
        String httpRoot = ResourcesHttpdTestServer.getHttpRoot();
        
        String httpURL = UMFileUtil.joinPaths(new String[] {httpRoot, 
            "phonepic-smaller.png"});
        
        //make sure that we can fetch a file
        String cachedFile = cacheDir.get(httpURL);
        Assert.assertTrue("Cached file downloaded", impl.fileExists(cachedFile));
        boolean savedOK = cacheDir.saveIndex();
        Assert.assertTrue("Cache dir can save OK", savedOK);
        
        cacheDir = new HTTPCacheDir(cacheDirName, null);
        String fileURI = cacheDir.getCacheFileURIByURL(httpURL);
        Assert.assertNotNull("HTTP URL is known on creating new cache dir object",
            fileURI);
        Assert.assertTrue("Cached file URI exists", impl.fileExists(fileURI));
        
        long date1 = HTTPCacheDir.parseHTTPDate("Sun, 06 Nov 1994 08:49:37 GMT");
        long date2 = HTTPCacheDir.parseHTTPDate("Sunday, 06-Nov-94 08:49:37 GMT");
        long date3 = HTTPCacheDir.parseHTTPDate("Sun Nov  6 08:49:37 1994 ");
        
        //The time in milliseconds between teh date given and the date calculated
        // accurate to within one second
        Assert.assertTrue("Can parse date 1", Math.abs(784111777137L -date1) <= 1000);
        Assert.assertTrue("Can parse date 2", Math.abs(784111777137L -date2) <= 1000);
        Assert.assertTrue("Can parse date 3", Math.abs(784111777137L -date3) <= 1000);

        Assert.assertEquals("Can format HTTP Date", "Sun, 06 Nov 1994 08:49:37 GMT",
            HTTPCacheDir.makeHTTPDate(784111777137L));
        
        
        
        //Expiry time in one hour
        long currentTime = System.currentTimeMillis();
        long expireHeaderTime = currentTime + (1000*60*60);
        Hashtable expireHeaders = new Hashtable();
        
        expireHeaders.put("expires", HTTPCacheDir.makeHTTPDate(expireHeaderTime));
        expireHeaders.put("cache-control", "max-age=7200");
        long calculatedExpiryTime = HTTPCacheDir.calculateExpiryTime(expireHeaders);
        long expectedExpiry = Math.abs(currentTime + (7200L*1000));
        //the cache-control should take precedence
        Assert.assertTrue("Can calculate expires time with both cache control and expires header",
            Math.abs(expectedExpiry - calculatedExpiryTime) < 1000);
        
        expireHeaders.remove("cache-control");
        calculatedExpiryTime = HTTPCacheDir.calculateExpiryTime(expireHeaders);
        Assert.assertTrue("Can calculate expires time with just expires header",
            Math.abs(expireHeaderTime - calculatedExpiryTime) < 1000);
        
        int numEntriesPreDelete = cacheDir.getNumEntries();
        boolean deletedEntry = cacheDir.deleteEntry(httpURL);
        Assert.assertTrue("Cache dir reports success deleting entry", deletedEntry);
        Assert.assertEquals("Cache dir now one entry smaller", numEntriesPreDelete-1,
            cacheDir.getNumEntries());
        
        try { Thread.sleep(1000); }
        catch(InterruptedException e) {}
        
        //access the entry
        cacheDir.get(httpURL);
        long timeSinceAccess = System.currentTimeMillis() - 
            cacheDir.getEntry(httpURL).getLong(HTTPCacheDir.IDX_LASTACCESSED);
        Assert.assertTrue("Last accessed time is updated", timeSinceAccess < 300);
        
        String httpURL2 = UMFileUtil.joinPaths(new String[] {httpRoot, 
            "smallcheck.jpg"});
        cacheDir.get(httpURL2);
        boolean oldestRemoved = cacheDir.removeOldestEntry();
        Assert.assertTrue("Can remove oldest entry", oldestRemoved);
        Assert.assertNotNull("most recently accessed item still in cache",
            cacheDir.getCacheFileURIByURL(httpURL2));

        /**
         * Test caching of private responses
         */
        cacheDir.saveIndex();
        String privateCacheDir = impl.getCacheDir(CatalogPresenter.USER_RESOURCE,
                PlatformTestUtil.getTargetContext());
        cacheDir.setPrivateCacheDir(privateCacheDir);
        String httpUrlPrivate = UMFileUtil.joinPaths(new String[]{httpRoot, "smallcheck.jpg?private=true"});
        String privateCacheFile = cacheDir.get(httpUrlPrivate);
        JSONArray cachePrivateEntry = cacheDir.getEntry(httpUrlPrivate);
        Assert.assertTrue("Entry with cache-control private marked as such",
                cachePrivateEntry.getBoolean(HTTPCacheDir.IDX_PRIVATE));
        Assert.assertTrue("Entry with cache-control private is in private directory",
                privateCacheFile.startsWith(privateCacheDir));
    }

    protected void runTest() throws Throwable {
        testHTTPCacheDir();
    }
    
}
