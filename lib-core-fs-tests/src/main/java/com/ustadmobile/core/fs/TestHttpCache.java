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
package com.ustadmobile.core.fs;

/* $if umplatform == 2  $
    import j2meunit.framework.TestCase;
 $else$ */
    /* $endif$ */

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.fs.db.HttpCacheDbEntry;
import com.ustadmobile.core.fs.db.HttpCacheDbManager;
import com.ustadmobile.core.impl.AbstractCacheResponse;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.HttpCacheEntry;
import com.ustadmobile.core.impl.HttpCacheResponse;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.UMTestUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import fi.iki.elonen.router.RouterNanoHTTPD;


/**
 *
 * @author mike
 */
public class TestHttpCache {
    

    private static RouterNanoHTTPD resourcesHttpd;

    private static HttpCache httpCache;

    static final String TEST_PNG_ASSET_RESOURCE = "/phonepic-smaller.png";

    public class UmHttpResponseNotifyCallback implements UmHttpResponseCallback {

        private UmHttpResponse response;

        private IOException exception;

        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            synchronized (this) {
                this.response = response;
                notifyAll();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            synchronized (this) {
                this.exception = exception;
                notifyAll();
            }
        }

        public UmHttpResponse getResponse() {
            return response;
        }

        public IOException getException() {
            return exception;
        }

        public synchronized UmHttpResponse waitAndGetResponse(int timeout) {
            waitForResponse(timeout);


            return response;
        }

        public void waitForResponse(int timeout) {
            synchronized (this) {
                if(response != null || exception != null)
                    return;

                try { this.wait(timeout); }
                catch(InterruptedException e) {}
            }
        }

        public IOException waitAndGetException(int timeout) {
            waitForResponse(timeout);

            return exception;
        }

        public synchronized void clear() {
            this.exception = null;
            this.response = null;
        }
    }



    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //context is not needed when we are asking for the shaerd cache dir
        String cacheDirName = impl.getCacheDir(CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
        httpCache = new HttpCache(cacheDirName);

    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    @Test
    public void testCacheAsync() throws Exception {
        Object context = PlatformTestUtil.getTargetContext();
        String httpRoot = ResourcesHttpdTestServer.getHttpRoot();
        UmHttpResponseNotifyCallback notifyCallback = new UmHttpResponseNotifyCallback();
        
        String httpURL = UMFileUtil.joinPaths(new String[] {httpRoot, 
            "phonepic-smaller.png"});


        httpCache.get(new UmHttpRequest(context, httpURL), notifyCallback);
        notifyCallback.waitAndGetResponse(240000);
        Assert.assertNotNull(notifyCallback.getResponse());
        Assert.assertTrue("InputStream bytes are identical to original resource",
                UMTestUtil.areStreamsEqual(getClass().getResourceAsStream(TEST_PNG_ASSET_RESOURCE),
                        notifyCallback.getResponse().getResponseAsStream()));
        Assert.assertEquals("Response code is 200", 200,
                notifyCallback.getResponse().getStatus());

        //asking for the body twice shoudl throw an exception
        Exception illegalStateException = null;
        try {
            InputStream secondStream = notifyCallback.getResponse().getResponseAsStream();
        }catch(IllegalStateException e) {
            illegalStateException = e;
        }
        Assert.assertNotNull("Illegal state exception thrown when body is requested twice",
                illegalStateException);


        //try making a request and using get response as bytes
        notifyCallback.clear();
        httpCache.deleteEntriesSync(context, new String[]{httpURL});
        httpCache.get(new UmHttpRequest(context, httpURL), notifyCallback);
        notifyCallback.waitForResponse(240000);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream resIn = getClass().getResourceAsStream(TEST_PNG_ASSET_RESOURCE);
        UMIOUtils.readFully(resIn, bout);
        byte[] responseBytes = notifyCallback.getResponse().getResponseBody();
        bout.flush();
        byte[] resourceBytes = bout.toByteArray();
        Assert.assertTrue("byte array from response is same as original resource",
                Arrays.equals(resourceBytes, responseBytes));

        //repeat the request and get response as bytes - should come from the cache (file stream)
        notifyCallback.clear();
        httpCache.get(new UmHttpRequest(context, httpURL), notifyCallback);
        notifyCallback.waitForResponse(240000);
        Assert.assertTrue("Second request is cache hit",
                ((AbstractCacheResponse)notifyCallback.getResponse()).isHit());
        Assert.assertTrue("Response bytes from cache equal original resource bytes",
                Arrays.equals(resourceBytes, notifyCallback.getResponse().getResponseBody()));

        //try it again and make sure it comes from the cache
        notifyCallback.clear();
        httpCache.get(new UmHttpRequest(context, httpURL), notifyCallback);
        notifyCallback.waitForResponse(240000);
        Assert.assertTrue("Response object is instanceof UmHttpCacheResponse",
                notifyCallback.getResponse() instanceof HttpCacheResponse);
        HttpCacheResponse cachedResponse = (HttpCacheResponse)notifyCallback.getResponse();
        Assert.assertTrue("Second request is a cache hit", cachedResponse.isHit());
        Assert.assertTrue("InputStream bytes are identical to original resource from cache hit",
                UMTestUtil.areStreamsEqual(getClass().getResourceAsStream(TEST_PNG_ASSET_RESOURCE),
                        notifyCallback.getResponse().getResponseAsStream()));
        Assert.assertTrue("File is cached on disk",
                new File(cachedResponse.getFileUri()).exists());

        //test 304 not modified validation
        notifyCallback.clear();
        httpCache.get(
                new UmHttpRequest(context, httpURL).addHeader("cache-control", "must-revalidate"),
                notifyCallback);
        notifyCallback.waitForResponse(240000);
        AbstractCacheResponse validatedResponse = (AbstractCacheResponse)notifyCallback.getResponse();
        Assert.assertEquals("Response is a validated HIT", AbstractCacheResponse.HIT_VALIDATED,
                validatedResponse.getCacheResponse());
        Assert.assertTrue("Response is marked as isNetworkResponseNotModified",
                validatedResponse.isNetworkResponseNotModified());

    }

    @Test
    public void testCacheSync() throws IOException {
        final Object context = PlatformTestUtil.getTargetContext();

        String httpURL = UMFileUtil.joinPaths(new String[] {ResourcesHttpdTestServer.getHttpRoot(),
                "phonepic-smaller.png"});
        UmHttpRequest request = new UmHttpRequest(context, httpURL);

        //make sure the entry is deleted before test runs
        httpCache.deleteEntriesSync(context, new String[]{httpURL});

        AbstractCacheResponse cacheResponse = (AbstractCacheResponse)httpCache.getSync(request);
        Assert.assertTrue("Byte data from response is the same",
                UMTestUtil.areStreamsEqual(getClass().getResourceAsStream(TEST_PNG_ASSET_RESOURCE),
                cacheResponse.getResponseAsStream()));
        Assert.assertTrue("Content-length is provided and > 0",
                Integer.parseInt(cacheResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH)) > 0);
    }

    /**
     * The HttpCacheDir is also expected to respond to requests for normal files (via file:// urls)
     * so that image components etc can simply make calls to the cache.
     */
    @Test
    public void testCacheFileResponse() throws IOException {
        Object context = PlatformTestUtil.getTargetContext();
        UmHttpResponseNotifyCallback notifyCallback = new UmHttpResponseNotifyCallback();

        //test that we can get a file from the disk using file:// requests
        InputStream resourceIn = getClass().getResourceAsStream("/phonepic-smaller.png");
        File testTmpFile = File.createTempFile("testhttpcache-test-file", ".png");
        OutputStream testTmpFileOut = new FileOutputStream(testTmpFile);
        UMIOUtils.readFully(resourceIn, testTmpFileOut);
        resourceIn.close();
        testTmpFileOut.close();

        notifyCallback.clear();

        httpCache.get(new UmHttpRequest(context, "file://" + testTmpFile.getAbsolutePath()), notifyCallback);
        AbstractCacheResponse fileResponse = (AbstractCacheResponse)notifyCallback.waitAndGetResponse(240000);
        Assert.assertEquals("File response is from same file as request", testTmpFile.getAbsolutePath(),
                fileResponse.getFileUri());
        Assert.assertEquals("File header content-type works from extension", "image/png",
                fileResponse.getHeader((UmHttpRequest.HEADER_CONTENT_TYPE)));
        Assert.assertEquals("File content-length if set according to file size",
                String.valueOf(testTmpFile.length()), fileResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH));
        Assert.assertTrue("File response is always considered fresh", fileResponse.isFresh());
        Assert.assertNull("Other file response headers are null",
                fileResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
        Assert.assertTrue("File response is successful", fileResponse.isSuccessful());
        Assert.assertEquals("File response status code is 200", 200, fileResponse.getStatus());

        FileInputStream testFileIn = new FileInputStream(testTmpFile);
        Assert.assertTrue("Stream delivered from response is the same as the file itself",
                UMTestUtil.areStreamsEqual(testFileIn, fileResponse.getResponseAsStream()));
        testFileIn.close();

        notifyCallback.clear();
        httpCache.get(new UmHttpRequest(context, "file://" + testTmpFile.getAbsolutePath()), notifyCallback);
        fileResponse = (AbstractCacheResponse)notifyCallback.waitAndGetResponse(240000);


        testFileIn = new FileInputStream(testTmpFile);
        Assert.assertTrue("Byte array delivered is the same as original file",
                UMTestUtil.areStreamsEqual(testFileIn, new ByteArrayInputStream(fileResponse.getResponseBody())));
    }


    @Test
    public void testOnlyIfCached() {
        Object context = PlatformTestUtil.getTargetContext();
        UmHttpResponseNotifyCallback notifyCallback = new UmHttpResponseNotifyCallback();
        String httpURL = UMFileUtil.joinPaths(new String[] {ResourcesHttpdTestServer.getHttpRoot(),
                "phonepic-smaller.png"});

        httpCache.deleteEntriesSync(context, new String[]{httpURL});

        httpCache.get(new UmHttpRequest(context, httpURL).setOnlyIfCached(true), notifyCallback);
        notifyCallback.waitAndGetException(240000);

        Assert.assertNull("Response to uncached entry, when using onlyIfCached, is null",
                notifyCallback.getResponse());
        Assert.assertNotNull("IOException occurred when requesting onlyIfCached entry that is not yet cached",
                notifyCallback.getException());

        notifyCallback.clear();
        httpCache.get(new UmHttpRequest(context, httpURL), notifyCallback);
        notifyCallback.waitForResponse(240000);

        notifyCallback.clear();
        httpCache.get(new UmHttpRequest(context, httpURL).setOnlyIfCached(true), notifyCallback);
        notifyCallback.waitForResponse(240000);
        Assert.assertTrue("Response to cached entry, when using onlyIfCached, is successful",
                notifyCallback.getResponse().isSuccessful());
    }

    @Test
    public void testFailedRequest() {
        Object context = PlatformTestUtil.getTargetContext();
        UmHttpResponseNotifyCallback notifyCallback = new UmHttpResponseNotifyCallback();
        String httpUrl = UMFileUtil.joinPaths(new String[] {ResourcesHttpdTestServer.getHttpRoot(),
                "does-not-exist.png"});
        httpCache.get(new UmHttpRequest(context, httpUrl), notifyCallback);
        notifyCallback.waitAndGetResponse(240000);

        Assert.assertTrue("Calling an error page results in response, isSuccessful = false",
                !notifyCallback.getResponse().isSuccessful());

        notifyCallback.clear();

        httpCache.get(new UmHttpRequest(context, "http://localhost:100/doesnotexist.html"), notifyCallback);
        notifyCallback.waitAndGetException(240000);
        Assert.assertNotNull("Connection refused results in a callback to onFailrue",
                notifyCallback.getException());
    }

    @Test
    public void testFreshness() {
        final Object context = PlatformTestUtil.getTargetContext();

        //Make up an entry that expires in 10 minutes, where it was last checked 9 minutes ago
        HttpCacheDbEntry dbEntry = HttpCacheDbManager.getInstance().makeNewEntry(context);
        dbEntry.setCacheControl("max-age=600");
        dbEntry.setLastChecked(System.currentTimeMillis() - (9 * 60 * 1000));
        HttpCacheEntry entry = new HttpCacheEntry(dbEntry);
        Assert.assertTrue("Cache-control header maxage=10min, last checked 9mins ago, isFresh",
                entry.isFresh(0));

        dbEntry.setLastChecked(System.currentTimeMillis() - (11 * 60 * 1000));
        Assert.assertTrue("Cache-control header maxage=10min, last checked 1mins ago, is stale",
                !entry.isFresh(0));

        //Test interpreting freshness using expires header
        dbEntry.setCacheControl(null);
        dbEntry.setExpiresTime(System.currentTimeMillis() + 1000);
        Assert.assertTrue("Entry is considered fresh if without cache-control header, with expires header in future",
                entry.isFresh());
        dbEntry.setExpiresTime(System.currentTimeMillis() - 1000);
        Assert.assertTrue("Entry is considered stale if without cache-control header, with expires header in past",
                !entry.isFresh());

        //test using time to live default
        dbEntry.setExpiresTime(-1);
        dbEntry.setLastChecked(System.currentTimeMillis());
        Assert.assertTrue("Entry without headers is considered fresh by default with a time to live",
                entry.isFresh(10000));
        Assert.assertTrue("Entry without headers is considered stale if no time to live given",
                !entry.isFresh(0));


    }

}
