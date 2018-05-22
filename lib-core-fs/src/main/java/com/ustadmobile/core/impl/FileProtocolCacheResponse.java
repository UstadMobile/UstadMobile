package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a cache 'response' that is in fact simply a wrapper for a file. This is used so that
 * the ImageLoader class can use the HttpCache, and thus load images from http or the file system
 */
public class FileProtocolCacheResponse extends AbstractCacheResponse {

    private File file;

    public FileProtocolCacheResponse(File file) {
        this.file = file;
    }

    @Override
    public String getHeader(String headerName) {
        switch (headerName) {
            case UmHttpRequest.HEADER_CONTENT_TYPE:
                return UstadMobileSystemImpl.getInstance().getMimeTypeFromExtension(
                        UMFileUtil.getExtension(file.getName()));
            case UmHttpRequest.HEADER_CONTENT_LENGTH:
                return String.valueOf(file.length());

            default:
                return null;
        }
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return UMIOUtils.readStreamToByteArray(new FileInputStream(file));
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public int getStatus() {
        return 200;
    }

    @Override
    public String getFileUri() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean isFresh(int timeToLive) {
        return true;
    }

    @Override
    public boolean isFresh() {
        return true;
    }
}
