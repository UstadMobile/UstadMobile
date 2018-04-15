package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Represents an Http Cache Response. There are three modes of delivering a response:
 *  1. If the data was already cached and the entry was fresh or validated by a 304 not modified:
 *      Data is served directly from the file the cache entry was stored in.
 *
 *  2. If the request was performed asynchronously then the cache response object will save the data
 *     to disk and retain the buffer (so it need not be read back from the disk). This is performed
 *     by saveNetworkResponseToDiskAndBuffer .
 *
 *  3. If the request was performed synchronously then initPipe should be called to setup piped input
 *     and output streams, and then pipeNetworkResponseToDisk should be called in a separate thread
 *     (eg. using an ExecutorService). The executor service will ensure the data is promptly written
 *     to disk regardless of whether or not the consumer which made the request processes it promptly.
 *
 *
 * Created by mike on 12/27/17.
 */

public class HttpCacheResponse extends AbstractCacheResponse implements Runnable{

    private HttpCachedEntry entry;

    private PipedInputStream bufferPipeIn;

    private PipedOutputStream bufferedPipeOut;

    private UmHttpResponse networkResponse;

    boolean bodyReturned = false;

    ResponseCompleteListener responseCompleteListener;

    private UmHttpRequest request;

    private int maxPipeBuffer = 2 * 1024 * 1024;

    private byte[] byteBuf;


    interface ResponseCompleteListener {
        void onResponseComplete(HttpCacheResponse response);
    }

    public HttpCacheResponse(HttpCachedEntry entry, UmHttpRequest request) {
        this.entry = entry;
        this.request = request;
        setCacheResponse(MISS);
    }



    protected void setNetworkResponse(UmHttpResponse response) {
        this.networkResponse = response;
    }

    protected UmHttpResponse getNetworkResponse() {
        return networkResponse;
    }

    public void run() {
        pipeNetworkResponseToDisk();
    }

    protected void initPipe() {
        String networkLengthHeader = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH);
        String networkEncodingHeader = networkResponse.getHeader(UmHttpRequest.HEADER_CONTENT_ENCODING);

        //if the content-length is provided and gzip encoding is not being used, then the maximum
        //pipe size we need is content-length.
        int pipeSize = maxPipeBuffer;
        if(networkLengthHeader != null && (networkEncodingHeader == null || networkEncodingHeader.equals("identity"))) {
            try {
                pipeSize = Math.min(maxPipeBuffer, Integer.parseInt(networkLengthHeader));
            }catch(NumberFormatException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 0, networkLengthHeader, e);
            }
        }

        bufferPipeIn = new PipedInputStream(pipeSize);

        try {
            bufferedPipeOut= new PipedOutputStream(bufferPipeIn);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0,
                    "HttpCacheResponse: Exception with pipe init");
        }
    }

    protected void pipeNetworkResponseToDisk() {
        InputStream networkIn = null;
        FileOutputStream fout = null;
        boolean responseCompleted = false;
        try {
            networkIn = networkResponse.getResponseAsStream();
            fout = new FileOutputStream(entry.getFileUri());

            byte[] buf = new byte[8*1024];
            int bytesRead;
            while((bytesRead = networkIn.read(buf)) != -1) {
                bufferedPipeOut.write(buf,0, bytesRead);
                fout.write(buf, 0, bytesRead);
            }

            fout.flush();
            bufferedPipeOut.flush();
            responseCompleted = true;
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Exception piping cache response to disk", e);
        }finally {
            UMIOUtils.closeInputStream(networkIn);
            UMIOUtils.closeOutputStream(fout);
            UMIOUtils.closeOutputStream(bufferedPipeOut);
        }

        if(responseCompleted && responseCompleteListener != null)
            responseCompleteListener.onResponseComplete(this);
    }

    protected void saveNetworkResponseToDiskAndBuffer() {
        FileOutputStream fout = null;
        boolean responseCompleted = false;
        try {
            byteBuf = networkResponse.getResponseBody();
            fout = new FileOutputStream(entry.getFileUri());
            fout.write(byteBuf);
            fout.flush();
            responseCompleted = true;
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Exception writing / buffering response", e);
        }finally {
            UMIOUtils.closeOutputStream(fout);
        }

        if(responseCompleted && responseCompleteListener != null)
            responseCompleteListener.onResponseComplete(this);
    }


    @Override
    public String getHeader(String headerName) {
        headerName = headerName.toLowerCase();
        switch(headerName){
            case UmHttpRequest.HEADER_CACHE_CONTROL:
                return entry.getCacheControl();
            case UmHttpRequest.HEADER_CONTENT_LENGTH:
                return String.valueOf(entry.getContentLength());
            case UmHttpRequest.HEADER_CONTENT_TYPE:
                return entry.getContentType();
            case UmHttpRequest.HEADER_ETAG:
                return entry.getEtag();
            case UmHttpRequest.HEADER_EXPIRES:
                return UMCalendarUtil.makeHTTPDate(entry.getExpiresTime());

            default:
                return null;
        }
    }

    private final void markBodyReturned(){
        if(bodyReturned)
            throw new IllegalStateException("HttpCacheResponse: Body already returned");

        bodyReturned = true;
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        if(!hasResponseBody())
            throw new IOException("getResponseBody called on response that has no body");

        markBodyReturned();
        if(networkResponse == null) {
            return UMIOUtils.readStreamToByteArray(UstadMobileSystemImpl.getInstance().openFileInputStream(
                    entry.getFileUri()));
        }else if(byteBuf != null) {
            return byteBuf;
        }else {
            return UMIOUtils.readStreamToByteArray(bufferPipeIn);
        }
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        if(!hasResponseBody())
            throw new IOException("getResponseAsStream called on response that has no body");

        markBodyReturned();
        if(networkResponse == null) {
            return UstadMobileSystemImpl.getInstance().openFileInputStream(entry.getFileUri());
        }else if(byteBuf != null){
            return new ByteArrayInputStream(byteBuf);
        }else {
            return bufferPipeIn;
        }
    }

    @Override
    public boolean isSuccessful() {
        return entry.getStatusCode() >= 200 && entry.getStatusCode() < 400;
    }

    @Override
    public int getStatus() {
        return entry.getStatusCode();
    }

    public String getFileUri() {
        return entry.getFileUri();
    }

    public void setOnResponseCompleteListener(ResponseCompleteListener responseCompleteListener) {
        this.responseCompleteListener = responseCompleteListener;
    }

    public HttpCachedEntry getEntry() {
        return entry;
    }

    public UmHttpRequest getRequest() {
        return request;
    }

    @Override
    public boolean isFresh(int timeToLive) {
        return HttpCache.isFresh(entry, timeToLive);
    }

    @Override
    public boolean isFresh() {
        return HttpCache.isFresh(entry);
    }

    /**
     * Single point to determine if this response has a request body.
     *
     * @return true if there is an http body attached with this request, false otherwise.
     */
    protected boolean hasResponseBody() {
        return !UmHttpRequest.METHOD_HEAD.equals(request.getMethod()) && entry.getStatusCode() != 204;
    }
}
