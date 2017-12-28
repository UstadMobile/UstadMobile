package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 12/27/17.
 */

public class HttpCacheResponse extends AbstractCacheResponse{

    private HttpCacheEntry entry;



    public HttpCacheResponse(HttpCacheEntry entry) {
        this.entry = entry;
        setCacheResponse(MISS);
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
                return entry.geteTag();
            case UmHttpRequest.HEADER_EXPIRES:
                return UMCalendarUtil.makeHTTPDate(entry.getExpiresTime());

            default:
                return null;
        }
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return UMIOUtils.readStreamToByteArray(UstadMobileSystemImpl.getInstance().openFileInputStream(
                entry.getFileUri()));
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        return UstadMobileSystemImpl.getInstance().openFileInputStream(entry.getFileUri());
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

    @Override
    public String getFilePath() {
        return entry.getFileUri();
    }
}
