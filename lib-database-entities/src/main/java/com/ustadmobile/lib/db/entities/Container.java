package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 51 , indices = {@UmIndex(
                name="cnt_uid_to_most_recent",
                value = {"containerContentEntryUid", "lastModified"})})
public class Container {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long containerUid;

    @UmSyncLocalChangeSeqNum
    private long cntLocalCsn;

    @UmSyncMasterChangeSeqNum
    private long cntMasterCsn;

    @UmSyncLastChangedBy
    private int cntLastModBy;

    private long fileSize;

    private long containerContentEntryUid;

    @UmIndexField
    private long lastModified;

    private String mimeType;

    private String remarks;

    private boolean mobileOptimized;

    /**
     * Total number of entries in this container
     */
    private int cntNumEntries;

    public Container() {

    }

    public Container(ContentEntry contentEntry) {
        this.containerContentEntryUid = contentEntry.getContentEntryUid();
    }

    public long getContainerUid() {
        return containerUid;
    }

    public void setContainerUid(long containerUid) {
        this.containerUid = containerUid;
    }


    public long getContainerContentEntryUid() {
        return containerContentEntryUid;
    }

    public void setContainerContentEntryUid(long containerContentEntryUid) {
        this.containerContentEntryUid = containerContentEntryUid;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    public long getCntLocalCsn() {
        return cntLocalCsn;
    }

    public void setCntLocalCsn(long cntLocalCsn) {
        this.cntLocalCsn = cntLocalCsn;
    }

    public long getCntMasterCsn() {
        return cntMasterCsn;
    }

    public void setCntMasterCsn(long cntMasterCsn) {
        this.cntMasterCsn = cntMasterCsn;
    }

    public int getCntLastModBy() {
        return cntLastModBy;
    }

    public void setCntLastModBy(int cntLastModBy) {
        this.cntLastModBy = cntLastModBy;
    }

    public int getCntNumEntries() {
        return cntNumEntries;
    }

    public void setCntNumEntries(int cntNumEntries) {
        this.cntNumEntries = cntNumEntries;
    }
}
