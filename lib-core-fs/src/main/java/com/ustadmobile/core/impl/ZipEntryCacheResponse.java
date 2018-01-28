package com.ustadmobile.core.impl;

import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 1/28/18.
 */

public class ZipEntryCacheResponse extends AbstractCacheResponse {

    private ZipEntry zipEntry;

    private ZipFile zipFile;

    private File file;

    private String entryPath;

    public ZipEntryCacheResponse(File file, String entryPath) {
        this.file = file;
        this.entryPath = entryPath;
        try {
            this.zipFile = new ZipFile(file);
            this.zipEntry = zipFile.getEntry(entryPath);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getHeader(String headerName) {
        switch (headerName) {
            case UmHttpRequest.HEADER_CONTENT_TYPE:
                return UstadMobileSystemImpl.getInstance().getMimeTypeFromExtension(
                        UMFileUtil.getExtension(zipEntry.getName()));
            case UmHttpRequest.HEADER_CONTENT_LENGTH:
                return String.valueOf(zipEntry.getSize());

            default:
                return null;
        }
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        if(zipEntry != null)
            return UMIOUtils.readStreamToByteArray(zipFile.getInputStream(zipEntry));
        else
            throw new IOException("Cannot ready entry: " + file.getAbsolutePath() + "!" + entryPath);
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        if(zipEntry != null)
            return zipFile.getInputStream(zipEntry);
        else
            throw new IOException("Cannot ready entry: " + file.getAbsolutePath() + "!" + entryPath);
    }

    @Override
    public boolean isSuccessful() {
        return zipEntry != null;
    }

    @Override
    public int getStatus() {
        return zipEntry != null ? 200 : 404;
    }

    @Override
    public String getFileUri() {
        return file.getAbsolutePath() + "!" + entryPath;
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
