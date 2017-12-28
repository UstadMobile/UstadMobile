package com.ustadmobile.core.impl;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.util.UMUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 12/26/17.
 *
 * The HttpCache provides transparent async http caching, as well as the ability to 'prime' the cache
 * by pre-loading it. It will be expanded to support saving cache entries in multiple directories so
 * that certain items can be 'subscribed' to and thus will not be eligible for deletion.
 *
 * The HttpCache will store etag and last modified information, and (mostly) obey http cache-control
 * and related headers.
 *
 * Placed in com.ustadmobile.core.impl so that the systemimpl makeRequestSync method can be
 * protected and accessed by this class, but it won't be accessible to other classes that
 * shouldn't use it (e.g. presenters).
 */
public class HttpCache {

    private String sharedDir;

    private String basePrivateDir;

    private JSONObject cacheDb;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private int defaultTimeToLive = 60* 1000;

    public static final String PROTOCOL_FILE = "file://";

    /**
     * Wrapper HttpCall object that can be used to cancel the request if required. It implements
     * runnable and can be invoked using the executor service.
     */
    private class UmHttpCacheCall extends UmHttpCall implements Runnable{

        protected UmHttpRequest request;

        //Actual http request, if an outgoing http request is actually sent
        private UmHttpRequest httpRequest;

        private UmHttpResponseCallback responseCallback;//End response callback

        //Handler that deals with the http request, if an outgoing request is actually sent
        private UmHttpResponseHandler httpResponseHandler;

        protected Hashtable requestCacheControl;

        protected HttpCacheEntry entry;

        private UmHttpCacheCall(UmHttpRequest request, UmHttpResponseCallback responseCallback) {
            this.request = request;
            this.responseCallback = responseCallback;
        }


        @Override
        public void run() {
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

            if(request.getUrl().startsWith(PROTOCOL_FILE)) {
                File responseFile = new File(UMFileUtil.stripPrefixIfPresent("file://",
                        request.getUrl()));
                responseCallback.onComplete(this, new FileProtocolCacheResponse(responseFile));
                return;
            }

            HttpCacheEntry entry = getEntry(request.getUrl());
            if(entry != null) {
                long expirationTime = calculateExpiryTime(entry.getCacheControl(), entry.getExpiresTime(),
                        entry.getLastChecked() + defaultTimeToLive);
                if(request.isOnlyIfCached() ||
                        !(request.mustRevalidate() && expirationTime > System.currentTimeMillis())) {
                    HttpCacheResponse cacheResponse = new HttpCacheResponse(entry);
                    cacheResponse.setCacheResponse(HttpCacheResponse.HIT_DIRECT);
                    //no validation required - directly return the cached response
                    responseCallback.onComplete(this, cacheResponse);
                    return;
                }
            }else if(request.isOnlyIfCached() && entry == null) {
                responseCallback.onFailure(this, new FileNotFoundException(request.getUrl()));
                return;
            }

            //make an http request for this cache entry
            httpRequest = new UmHttpRequest(request.getUrl());
            if(entry != null) {
                if(entry.geteTag() != null) {
                    httpRequest.addHeader("if-none-match", entry.geteTag());
                }
                if(entry.getLastModified() > 0) {
                    httpRequest.addHeader("if-modified-since",
                            UMUtil.makeHTTPDate(entry.getLastModified()));
                }
            }

            httpResponseHandler = new UmHttpResponseHandler(this);
            impl.sendRequestAsync(httpRequest, httpResponseHandler);
        }


        @Override
        public void cancel() {
            //TODO: implement cancel
        }
    }

    /**
     * The HttpResponseHandler handles the response from the network after UmHttpCacheCall makes
     * an http call (if needed).
     */
    private class UmHttpResponseHandler implements Runnable, UmHttpResponseCallback {

        private UmHttpCacheCall cacheCall;

        private UmHttpResponse response;

        private UmHttpResponseHandler(UmHttpCacheCall cacheCall) {
            this.cacheCall = cacheCall;
        }

        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            this.response = response;
            executorService.execute(this);
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            cacheCall.responseCallback.onFailure(cacheCall, exception);
        }

        @Override
        public void run() {
            HttpCacheResponse cacheResponse = cacheResponse(cacheCall.request, response);
            cacheCall.responseCallback.onComplete(cacheCall, cacheResponse);
        }
    }



    public HttpCache(String sharedDir) {
        this.sharedDir = sharedDir;
        initCache();
    }

    protected void initCache() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        InputStream fileIndexIn = null;
        try {
            if(!impl.fileExists(sharedDir)) {
                impl.makeDirectoryRecursive(sharedDir);
            }

            String sharedIndexPath = getSharedIndexPath();

            if(impl.fileExists(sharedIndexPath)) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                fileIndexIn = impl.openFileInputStream(sharedIndexPath);
                UMIOUtils.readFully(fileIndexIn, bout);
                cacheDb = new JSONObject(new String(bout.toByteArray(), "UTF-8"));
            }else {
                cacheDb = new JSONObject();
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 4, sharedDir, e);
        }finally {
            UMIOUtils.closeInputStream(fileIndexIn);
        }
    }

    protected String getSharedIndexPath() {
        return UMFileUtil.joinPaths(new String[]{sharedDir, "cache_index.json"});
    }

    public UmHttpCall get(UmHttpRequest request, UmHttpResponseCallback callback) {
        UmHttpCacheCall cacheCall = new UmHttpCacheCall(request, callback);
        executorService.execute(cacheCall);
        return cacheCall;
    }


    private HttpCacheEntry getEntry(String url) {
        JSONObject entryObj = cacheDb.optJSONObject(url);
        HttpCacheEntry entry = null;
        if(entryObj != null) {
            entry = new HttpCacheEntry();
            entry.loadFromJson(entryObj);
        }

        return entry;
    }

    public HttpCacheResponse cacheResponse(UmHttpRequest request, UmHttpResponse response) {
        HttpCacheEntry entry = getEntry(request.getUrl());
        if(entry == null) {
            entry = new HttpCacheEntry();
            entry.setFileUri(generateCacheEntryFileName(request, response, sharedDir));
        }

        HttpCacheResponse cacheResponse = new HttpCacheResponse(entry);

        if(response.getStatus() != 304) {
            cacheResponse.setCacheResponse(HttpCacheResponse.MISS);
            FileOutputStream fout = null;
            InputStream responseIn = null;
            try {
                fout = new FileOutputStream(entry.getFileUri());
                responseIn = response.getResponseAsStream();
                UMIOUtils.readFully(responseIn, fout);
            }catch(IOException e){
                UstadMobileSystemImpl.l(UMLog.ERROR, 66, null, e);
            }finally {
                UMIOUtils.closeInputStream(responseIn);
                UMIOUtils.closeOutputStream(fout);
            }
        }else {
            cacheResponse.setCacheResponse(HttpCacheResponse.HIT_VALIDATED);
        }

        long currentTime = System.currentTimeMillis();
        entry.updateFromResponse(response);
        entry.setLastAccessed(currentTime);
        entry.setLastChecked(currentTime);

        cacheDb.put(request.getUrl(), entry.toJson());

        return cacheResponse;
    }

    private String generateCacheEntryFileName(UmHttpRequest request, UmHttpResponse response,
                                              String dir) {
        File dirFile = new File(dir);
        File entryFile;
        String filename = CatalogPresenter.sanitizeIDForFilename(
                UMFileUtil.getFilename(request.getUrl()));

        String[] filenameParts = UMFileUtil.splitFilename(filename);
        String contentType = response.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE);
        if(contentType != null) {
            String expectedExtension = UstadMobileSystemImpl.getInstance().getExtensionFromMimeType(contentType);
            if(expectedExtension != null && !expectedExtension.equals(filenameParts[1])) {
                filenameParts = new String[]{filename, expectedExtension};
                filename = filenameParts[0] + "." + filenameParts[1];
            }
        }

        entryFile = new File(dirFile, filename);
        if(!entryFile.exists()) {
            return entryFile.getAbsolutePath();
        }

        //try and get to a unique suffix
        for(int i = 0; i < 100; i++) {
            entryFile = new File(dir, filenameParts[0] + i + '.' + filenameParts[1]);
            if(!entryFile.exists())
                return entryFile.getAbsolutePath();
        }

        return UMUUID.randomUUID().toString() + "." + filenameParts[1];
    }

    /**
     * Calculates when an entry will expire based on it's HTTP headers: specifically
     * the expires header and cache-control header
     *
     * As per :  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section
     * 14.9.3 the max-age if present will take precedence over the expires header
     *
     *
     * @param cacheControlHeader Cache control header value
     * @param expires value (in utime) of the expires header
     *
     * @param defaultVal Expiry value to use in case headers do not contain max-age or expires
     *
     * @return
     */
    public static long calculateExpiryTime(String cacheControlHeader, long expires, long defaultVal) {
        if(cacheControlHeader != null) {
            Hashtable ccParams = UMFileUtil.parseParams(cacheControlHeader, ',');
            if(ccParams.containsKey("max-age")) {
                long maxage = Integer.parseInt((String)ccParams.get("max-age"));
                return System.currentTimeMillis() + (maxage * 1000);
            }
        }

        if(expires >= 0) {
            return expires;
        }

        return defaultVal;
    }


}
