package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpResponse;

import java.io.IOException;
import java.io.InputStream;

public class NoCacheResponse extends AbstractCacheResponse {

    private UmHttpResponse networkResponse;

    public NoCacheResponse(UmHttpResponse networkResponse) {
        this.networkResponse = networkResponse;
        setCacheResponse(MISS);
    }

    @Override
    public String getFileUri() {
        return null;
    }

    @Override
    public boolean isFresh(int timeToLive) {
        return true;
    }

    @Override
    public boolean isFresh() {
        return true;
    }

    @Override
    public String getHeader(String headerName) {
        return networkResponse.getHeader(headerName);
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return networkResponse.getResponseBody();
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        return networkResponse.getResponseAsStream();
    }

    @Override
    public boolean isSuccessful() {
        return networkResponse.isSuccessful();
    }

    @Override
    public int getStatus() {
        return networkResponse.getStatus();
    }
}
