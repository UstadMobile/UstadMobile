package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContentEntry implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long contentEntryUid;

    private String entryId;

    private String mimeType;

    private String title;

    private long lastModified;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    public long getContentEntryUid() {
        return contentEntryUid;
    }

    public void setContentEntryUid(long contentEntryUid) {
        this.contentEntryUid = contentEntryUid;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }

    @Override
    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
    }
}
