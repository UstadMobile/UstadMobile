package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a file that contains one or more ContentEntry . This file could be an EPUB, xAPI Zip,
 * etc.
 */
@UmEntity
public class ContentEntryFile {

    @UmPrimaryKey(autoIncrement = true)
    private long contentEntryFileUid;

    private long fileSize;

    private String md5sum;

    private long lastModified;

    private String mimeType;

    private String remarks;

    private boolean mobileOptimized;


    public long getContentEntryFileUid() {
        return contentEntryFileUid;
    }

    public void setContentEntryFileUid(long contentEntryFileUid) {
        this.contentEntryFileUid = contentEntryFileUid;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isMobileOptimized() {
        return mobileOptimized;
    }

    public void setMobileOptimized(boolean mobileOptimized) {
        this.mobileOptimized = mobileOptimized;
    }
}
