package com.ustadmobile.core.impl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 12/26/17.
 *
 * The HttpCache provides transparent synchronous and async http caching, as well as the ability to
 * 'prime' the cache by pre-loading it. It will be expanded to support saving cache entries in multiple
 * directories so that certain items can be 'subscribed' to and thus will not be eligible for deletion.
 *
 * The HttpCache will store etag and last modified information, and (mostly) obey http cache-control
 * and related headers.
 *
 * Placed in com.ustadmobile.core.impl so that the systemimpl makeRequestSync method can be
 * protected and accessed by this class, but it won't be accessible to other classes that
 * shouldn't use it (e.g. presenters).
 *
 * TODO: Answer HTTP head request with information from a cached GET request
 */
public class HttpCache implements HttpCacheResponse.ResponseCompleteListener{

    private String sharedDir;

    private String basePrivateDir;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static final int DEFAULT_TIME_TO_LIVE = (60 * 60 * 1000);

    private int defaultTimeToLive = DEFAULT_TIME_TO_LIVE;

    public static final String PROTOCOL_FILE = "file://";

    public static final String CACHE_CONTROL_KEY_MAX_AGE = "max-age";

    public static final String CACHE_CONTROL_NO_CACHE = "no-cache";

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

        protected HttpCachedEntry entry;

        private boolean async = true;

        private UmHttpCacheCall(UmHttpRequest request, UmHttpResponseCallback responseCallback) {
            this.request = request;
            this.responseCallback = responseCallback;

            async = (responseCallback != null);
        }

        public UmHttpResponse execute() throws IOException {
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            AbstractCacheResponse cacheResponse = null;

            if(request.getUrl().startsWith(PROTOCOL_FILE)) {
                String filePath = UMFileUtil.stripPrefixIfPresent("file://", request.getUrl());
                int zipSepPos = filePath.indexOf('!');
                if(zipSepPos == -1) {
                    File responseFile = new File(filePath);
                    cacheResponse = new FileProtocolCacheResponse(responseFile);
                    if(async) {
                        responseCallback.onComplete(this, cacheResponse);
                    }
                }else {
                    cacheResponse = new ZipEntryCacheResponse(
                            new File(filePath.substring(0, zipSepPos)),
                            filePath.substring(zipSepPos+1));
                    if(async){
                        responseCallback.onComplete(this, cacheResponse);
                    }
                }

                return cacheResponse;
            }

            HttpCachedEntry entry = getEntry(request.getContext(), request.getUrl(), request.getMethod());
            if(entry != null) {
                int timeToLive = request.mustRevalidate() ? 0 : defaultTimeToLive;
                if(isFresh(entry, timeToLive) || request.isOnlyIfCached()) {
                    cacheResponse = new HttpCacheResponse(entry, request);
                    cacheResponse.setCacheResponse(HttpCacheResponse.HIT_DIRECT);
                    UstadMobileSystemImpl.l(UMLog.INFO, 384, "Cache:HIT_DIRECT: "
                            + request.getUrl());

                    //no validation required - directly return the cached response
                    if(async) {
                        responseCallback.onComplete(this, cacheResponse);
                    }
                    return cacheResponse;

                }
            }else if(request.isOnlyIfCached() && entry == null) {
                IOException ioe =  new FileNotFoundException(request.getUrl());
                if(async) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 386,
                            "Cache:onlyIfCached: Fail: not cached: " + request.getUrl());
                    responseCallback.onFailure(this, ioe);
                    return cacheResponse;
                }else {
                    throw ioe;
                }
            }

            //make an http request for this cache entry
            httpRequest = new UmHttpRequest(request.getContext(), request.getUrl());
            if(entry != null) {
                if(entry.getEtag() != null) {
                    httpRequest.addHeader("if-none-match", entry.getEtag());
                }
                if(entry.getLastModified() > 0) {
                    httpRequest.addHeader("if-modified-since",
                            UMCalendarUtil.makeHTTPDate(entry.getLastModified()));
                }
            }

            httpResponseHandler = new UmHttpResponseHandler(this);
            if(async) {
                impl.sendRequestAsync(httpRequest, httpResponseHandler);
            }else {
                httpResponseHandler.response = impl.sendRequestSync(httpRequest);
                return httpResponseHandler.execute();

            }

            return null;
        }

        @Override
        public void run() {
            try {
                execute();
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 73, request.getUrl(), e);
            }
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
            execute();
        }

        public AbstractCacheResponse execute() {
            String responseCacheControlHeader = response.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL);

            if(responseCacheControlHeader != null) {
                Hashtable responseCacheControl = UMFileUtil.parseParams(responseCacheControlHeader, ',');

                if(responseCacheControl.containsKey(CACHE_CONTROL_NO_CACHE)) {
                    AbstractCacheResponse noCacheResponse = new NoCacheResponse(response);
                    if(cacheCall.async) {
                        cacheCall.responseCallback.onComplete(cacheCall, noCacheResponse);
                    }
                    return noCacheResponse;
                }
            }

            HttpCacheResponse cacheResponse = cacheResponse(cacheCall.request, response, !cacheCall.async);

            if(cacheCall.async) {
                cacheCall.responseCallback.onComplete(cacheCall, cacheResponse);
            }

            return cacheResponse;
        }
    }

    private class DeleteEntriesTask implements Runnable {

        private String[] urlsToDelete;

        private Object context;

        private UmCallback callback;

        private DeleteEntriesTask(Object context, String[] urlsToDelete, UmCallback callback) {
            this.context = context;
            this.urlsToDelete = urlsToDelete;
            this.callback = callback;
        }

        @Override
        public void run() {
            List<String> fileUrisToDelete = UmAppDatabase.getInstance(context).getHttpCachedEntryDao()
                    .findFileUrisByUrl(Arrays.asList(urlsToDelete));
            List<String> deletedFileUris = new ArrayList<>();
            File entryFile;
            for(String fileUri : fileUrisToDelete) {
                entryFile = new File(fileUri);
                if(!entryFile.exists() || (entryFile.exists() && entryFile.delete())){
                    deletedFileUris.add(fileUri);
                }else {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Failed to delete cache file: " +
                            fileUri);
                }
            }

            UmAppDatabase.getInstance(context).getHttpCachedEntryDao().deleteByFileUris(deletedFileUris);

            if(callback != null)
                callback.onSuccess(null);
        }
    }




    public HttpCache(String sharedDir) {
        this.sharedDir = sharedDir;
        initCache();
    }

    protected void initCache() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        InputStream fileIndexIn = null;
//        This class is going to be removed anyway and replaced with using the image library caching mechanisms
//        try {
//            if(!new File(sharedDir).exists()) {
//                impl.makeDirectoryRecursive(sharedDir);
//            }
//        }catch(IOException e) {
//            UstadMobileSystemImpl.l(UMLog.CRITICAL, 4, sharedDir, e);
//        }finally {
//            UMIOUtils.closeInputStream(fileIndexIn);
//        }
    }

    public UmHttpCall get(UmHttpRequest request, UmHttpResponseCallback callback) {
        UmHttpCacheCall cacheCall = new UmHttpCacheCall(request, callback);
        executorService.execute(cacheCall);
        return cacheCall;
    }

    /**
     * Performs an http request synchronously. Returns as soon as the the connection is established.
     * The response will be simultaneously saved to the disk as it's read. If the response comes from
     * the network, this is done using an executor to fork a thread that simultaneously writes to a
     * fileoutputstream to cache the entry to disk and writes to a pipedoutputstream, which connects
     * with a pipedinputstream providing the response to the consumer.
     *
     * The client is expected to read the entire response. Failing to do so could cause an issue as
     * the pipedstream has a limited buffer size.
     *
     * @param request HttpRequest object for the request
     *
     * @return AbstractCacheResponse object representing the http response.
     * @throws IOException If an IOException occurs
     */
    public UmHttpResponse getSync(UmHttpRequest request) throws IOException{
        UmHttpCacheCall cacheCall = new UmHttpCacheCall(request, null);
        return cacheCall.execute();
    }

    private HttpCachedEntry getEntry(Object context, String url, String method) {
        return UmAppDatabase.getInstance(context).getHttpCachedEntryDao()
                .findByUrlAndMethod(url, getMethodFlag(method));
    }

    public HttpCacheResponse cacheResponse(UmHttpRequest request, UmHttpResponse networkResponse,
                                           boolean forkSaveToDisk) {
        final String requestUrl = request.getUrl();
        HttpCachedEntry entry = getEntry(request.getContext(), requestUrl, request.getMethod());
        boolean responseHasBody = !UmHttpRequest.METHOD_HEAD.equals(request.getMethod())
                && networkResponse.getStatus() != 204;

        if(entry == null) {
            entry = new HttpCachedEntry();
            entry.setUrl(requestUrl);
            if(responseHasBody) {
                entry.setFileUri(generateCacheEntryFileName(request, networkResponse, sharedDir));
            }
        }

        final HttpCacheResponse cacheResponse = new HttpCacheResponse(entry, request);
        cacheResponse.setNetworkResponse(networkResponse);
        cacheResponse.getEntry().setLastChecked(System.currentTimeMillis());
        updateCachedEntryFromNetworkResponse(cacheResponse.getEntry(), networkResponse);


        if(networkResponse.getStatus() == 304) {
            updateCacheIndex(cacheResponse);
            cacheResponse.setNetworkResponseNotModified(true);
            UstadMobileSystemImpl.l(UMLog.INFO, 387, "Cache:HIT_VALIDATED:"+ request.getUrl());
        }else if(responseHasBody){
            cacheResponse.setOnResponseCompleteListener(this);
            UstadMobileSystemImpl.l(UMLog.INFO, 385, "Cache:MISS - storing:"+ request.getUrl());
            if(forkSaveToDisk) {
                cacheResponse.initPipe();
                executorService.execute(cacheResponse);
            }else {
                cacheResponse.saveNetworkResponseToDiskAndBuffer();
            }
        }

        return cacheResponse;
    }

    /**
     * Update the given HttpCachedEntry from the response received over the network. This can't be
     * part of the entry  itself, because the entry is in the database module. This will only update
     * the object itself and will NOT persist it to the database
     *
     * @param cachedEntry The cached entry to update
     * @param networkResponse The network response just received from the network
     */
    public static void updateCachedEntryFromNetworkResponse(HttpCachedEntry cachedEntry, UmHttpResponse networkResponse) {
        String headerVal;
        if(networkResponse.getStatus() != 304) {
            //new entry was downloaded - update the length etc.
            headerVal = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH);
            if(headerVal != null) {
                try {
                    cachedEntry.setContentLength(Integer.parseInt(headerVal));
                }catch(IllegalArgumentException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 74, headerVal, e);
                }
            }

            cachedEntry.setStatusCode(networkResponse.getStatus());
        }

        cachedEntry.setCacheControl(networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
        cachedEntry.setContentType(networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        cachedEntry.setExpiresTime(convertDateHeaderToLong(UmHttpRequest.HEADER_EXPIRES, networkResponse));
        cachedEntry.setContentType(networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_TYPE));
        cachedEntry.setEtag(networkResponse.getHeader(UmHttpRequest.HEADER_ETAG));
        cachedEntry.setCacheControl(networkResponse.getHeader(UmHttpRequest.HEADER_CACHE_CONTROL));
    }

    /**
     * Determine if the entry is considered fresh.
     *
     * @see #calculateEntryExpirationTime(HttpCachedEntry)
     *
     * @param timeToLive the time since when this entry was last checked for which the entry will be
     *                   considered fresh if the cache-control headers and expires headers do not
     *                   provide this information.
     *
     * @return true if the entry is considered fresh, false otherwise
     */
    public static boolean isFresh(HttpCachedEntry cachedEntry, int timeToLive) {
        long expiryTime = calculateEntryExpirationTime(cachedEntry);
        long timeNow = System.currentTimeMillis();

        if(expiryTime != -1) {
            return expiryTime > timeNow;
        }else {
            return cachedEntry.getLastChecked() + timeToLive > timeNow;
        }
    }

    public static boolean isFresh(HttpCachedEntry cachedEntry) {
        return isFresh(cachedEntry, DEFAULT_TIME_TO_LIVE);
    }

    /**
     * Calculates when an entry will expire based on it's HTTP headers: specifically
     * the expires header and cache-control header
     *
     * As per :  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section
     * 14.9.3 the max-age if present will take precedence over the expires header
     *
     * @return -1 if the expiration time calculated from the headers provided if possible, -1 otherwise
     */
    public static final long calculateEntryExpirationTime(HttpCachedEntry cachedEntry) {
        String cacheControl = cachedEntry.getCacheControl();
        if(cacheControl != null) {
            Hashtable ccParams = UMFileUtil.parseParams(cacheControl, ',');
            if(ccParams.containsKey(CACHE_CONTROL_KEY_MAX_AGE)) {
                long maxage = Integer.parseInt((String)ccParams.get(CACHE_CONTROL_KEY_MAX_AGE));
                return cachedEntry.getLastChecked() + (maxage * 1000);
            }
        }

        if(cachedEntry.getExpiresTime() >= 0) {
            return cachedEntry.getExpiresTime();
        }

        return -1;
    }


    private static long convertDateHeaderToLong(String headerName, UmHttpResponse response) {
        String headerVal = response.getHeader(headerName);
        if(headerVal != null) {
            try {
                return UMCalendarUtil.parseHTTPDate(headerVal);
            }catch(NumberFormatException e) {
                return -1L;
            }
        }else {
            return -1L;
        }
    }


    public void deleteEntries(Object context, String[] urls, UmCallback callback){
        executorService.execute(new DeleteEntriesTask(context, urls, callback));
    }

    public void deleteEntriesSync(Object context, String[] urls) {
        new DeleteEntriesTask(context, urls, null).run();
    }

    protected void updateCacheIndex(HttpCacheResponse response) {
        if(response.getNetworkResponse().getStatus() == 304) {
            response.setCacheResponse(HttpCacheResponse.HIT_VALIDATED);
        }

        response.getEntry().setLastAccessed(System.currentTimeMillis());
        UmAppDatabase.getInstance(response.getRequest().getContext()).getHttpCachedEntryDao()
                .insert(response.getEntry());
    }

    @Override
    public void onResponseComplete(HttpCacheResponse response) {
        updateCacheIndex(response);
    }



    private String generateCacheEntryFileName(UmHttpRequest request, UmHttpResponse response,
                                              String dir) {
        File dirFile = new File(dir);
        File entryFile;
        String filename = UMIOUtils.sanitizeIDForFilename(
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

        return new File(dir, UMUUID.randomUUID().toString() + "." + filenameParts[1]).getAbsolutePath();
    }

    /**
     * Get the METHOD_ integer flag for the given HTTP Method as a string
     *
     * @param methodName The HTTP method as a string e.g. "GET", "HEAD", "POST"
     *
     * @return
     */
    public static int getMethodFlag(String methodName) {
        if(methodName.equalsIgnoreCase(UmHttpRequest.METHOD_GET))
            return HttpCachedEntry.METHOD_GET;
        else if(methodName.equalsIgnoreCase(UmHttpRequest.METHOD_HEAD))
            return HttpCachedEntry.METHOD_HEAD;
        else if(methodName.equalsIgnoreCase(UmHttpRequest.METHOD_POST))
            return HttpCachedEntry.METHOD_POST;

        return -1;
    }

}
