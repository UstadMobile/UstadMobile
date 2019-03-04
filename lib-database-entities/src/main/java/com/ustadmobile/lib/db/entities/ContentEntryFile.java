package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntryFile.TABLE_ID;


/**
 * Represents a file that contains one or more ContentEntry . This file could be an EPUB, xAPI Zip,
 * etc.
 *
 * Deprecated: this is being replaced with Container which support de-duplicating entries
 */
@Deprecated
@UmEntity(tableId = TABLE_ID)
public class ContentEntryFile {

    public static final int TABLE_ID = 5;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long contentEntryFileUid;

    private long fileSize;

    private String md5sum;

    @UmIndexField
    private long lastModified;

    private String mimeType;

    private String remarks;

    private boolean mobileOptimized;

    @UmSyncLocalChangeSeqNum
    private long contentEntryFileLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long contentEntryFileMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int contentEntryFileLastChangedBy;

    public ContentEntryFile(){

    }

    public ContentEntryFile(long fileSize) {
        this.fileSize = fileSize;
    }

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

    public long getContentEntryFileLocalChangeSeqNum() {
        return contentEntryFileLocalChangeSeqNum;
    }

    public void setContentEntryFileLocalChangeSeqNum(long contentEntryFileLocalChangeSeqNum) {
        this.contentEntryFileLocalChangeSeqNum = contentEntryFileLocalChangeSeqNum;
    }

    public long getContentEntryFileMasterChangeSeqNum() {
        return contentEntryFileMasterChangeSeqNum;
    }

    public void setContentEntryFileMasterChangeSeqNum(long contentEntryFileMasterChangeSeqNum) {
        this.contentEntryFileMasterChangeSeqNum = contentEntryFileMasterChangeSeqNum;
    }

    public int getContentEntryFileLastChangedBy() {
        return contentEntryFileLastChangedBy;
    }

    public void setContentEntryFileLastChangedBy(int contentEntryFileLastChangedBy) {
        this.contentEntryFileLastChangedBy = contentEntryFileLastChangedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryFile that = (ContentEntryFile) o;

        if (contentEntryFileUid != that.contentEntryFileUid) return false;
        if (fileSize != that.fileSize) return false;
        if (lastModified != that.lastModified) return false;
        if (mobileOptimized != that.mobileOptimized) return false;
        if (md5sum != null ? !md5sum.equals(that.md5sum) : that.md5sum != null) return false;
        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null)
            return false;
        return remarks != null ? remarks.equals(that.remarks) : that.remarks == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (contentEntryFileUid ^ (contentEntryFileUid >>> 32));
        result = 31 * result + (int) (fileSize ^ (fileSize >>> 32));
        result = 31 * result + (md5sum != null ? md5sum.hashCode() : 0);
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (mobileOptimized ? 1 : 0);
        return result;
    }
}
