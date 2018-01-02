package com.ustadmobile.core.impl;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.fs.db.HttpCacheDbEntry;
import com.ustadmobile.core.fs.db.HttpCacheDbManager;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUUID;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
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
 */
public class HttpCache implements HttpCacheResponse.ResponseCompleteListener{

    private String sharedDir;

    private String basePrivateDir;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private int defaultTimeToLive = 60* 60 * 1000;

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

        protected HttpCacheEntry entry;

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
                File responseFile = new File(UMFileUtil.stripPrefixIfPresent("file://",
                        request.getUrl()));
                cacheResponse = new FileProtocolCacheResponse(responseFile);
                if(async) {
                    responseCallback.onComplete(this, cacheResponse);
                }

                return cacheResponse;
            }

            HttpCacheEntry entry = getEntry(request.getContext(), request.getUrl());
            if(entry != null) {
                int timeToLive = request.mustRevalidate() ? 0 : defaultTimeToLive;
                if(entry.isFresh(timeToLive) || request.isOnlyIfCached()) {
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
                if(entry.geteTag() != null) {
                    httpRequest.addHeader("if-none-match", entry.geteTag());
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
            final HttpCacheDbManager dbManager = HttpCacheDbManager.getInstance();
            HttpCacheDbEntry entry;
            File entryFile;

            for(int i = 0; i < urlsToDelete.length; i++) {
                entry = dbManager.getEntryByUrl(context, urlsToDelete[i]);
                if(entry == null)
                    continue;

                entryFile= new File(entry.getFileUri());
                if(entryFile.exists()) {
                    entryFile.delete();
                }

                dbManager.delete(context, entry);
            }

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
        try {
            if(!impl.fileExists(sharedDir)) {
                impl.makeDirectoryRecursive(sharedDir);
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.CRITICAL, 4, sharedDir, e);
        }finally {
            UMIOUtils.closeInputStream(fileIndexIn);
        }
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


    private HttpCacheEntry getEntry(Object context, String url) {
        HttpCacheDbEntry dbEntry  = HttpCacheDbManager.getInstance().getEntryByUrl(context, url);
        if(dbEntry != null) {
            return new HttpCacheEntry(dbEntry);
        }else {
            return null;
        }
    }

    public HttpCacheResponse cacheResponse(UmHttpRequest request, UmHttpResponse networkResponse,
                                           boolean forkSaveToDisk) {
        final String requestUrl = request.getUrl();
        HttpCacheEntry entry = getEntry(request.getContext(), requestUrl);
        if(entry == null) {
            entry = new HttpCacheEntry(HttpCacheDbManager.getInstance().makeNewEntry(
                    request.getContext()));
            entry.setUrl(requestUrl);
            entry.setFileUri(generateCacheEntryFileName(request, networkResponse, sharedDir));
        }

        final HttpCacheResponse cacheResponse = new HttpCacheResponse(entry, request);
        cacheResponse.setNetworkResponse(networkResponse);
        cacheResponse.getEntry().setLastChecked(System.currentTimeMillis());
        cacheResponse.getEntry().updateFromResponse(networkResponse);

        if(networkResponse.getStatus() == 304) {
            updateCacheIndex(cacheResponse);
            cacheResponse.setNetworkResponseNotModified(true);
            UstadMobileSystemImpl.l(UMLog.INFO, 387, "Cache:HIT_VALIDATED:"+ request.getUrl());
        }else {
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
        HttpCacheDbManager.getInstance().persist(response.getRequest().getContext(),
                response.getEntry().getDbEntry());
    }

    @Override
    public void onResponseComplete(HttpCacheResponse response) {
        updateCacheIndex(response);
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

}
