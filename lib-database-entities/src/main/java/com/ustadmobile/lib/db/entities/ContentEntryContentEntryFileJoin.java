package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin.TABLE_ID;


/**
 * Join Entry to link a ContentEntry to a ContentEntryFile that actually contains the content. That
 * file can then be downloaded using an endpoint.
 *
 * Deprecated: this is being replaced with Container which support de-duplicating entries
 *
 */
//short code cecefj
@Deprecated
@UmEntity(tableId = TABLE_ID)
public class ContentEntryContentEntryFileJoin {

    public static final int TABLE_ID = 4;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long cecefjUid;

    @UmIndexField
    private long cecefjContentEntryUid;

    @UmIndexField
    private long cecefjContentEntryFileUid;

    @UmIndexField
    private long cecefjContainerUid;

    @UmSyncLocalChangeSeqNum
    private long cecefjLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long cecefjMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int cecefjLastChangedBy;

    public ContentEntryContentEntryFileJoin() {

    }

    public ContentEntryContentEntryFileJoin(ContentEntry entry, ContentEntryFile contentEntryFile) {
        this.cecefjContentEntryUid = entry.getContentEntryUid();
        this.cecefjContentEntryFileUid = contentEntryFile.getContentEntryFileUid();
    }

    public ContentEntryContentEntryFileJoin(long joinId, long contentEntryId, long contentEntryFileUid, long cecefjContainerUid) {
        this.cecefjUid = joinId;
        this.cecefjContentEntryUid = contentEntryId;
        this.cecefjContentEntryFileUid = contentEntryFileUid;
        this.cecefjContainerUid = cecefjContainerUid;
    }


    public long getCecefjUid() {
        return cecefjUid;
    }

    public void setCecefjUid(long cecefjUid) {
        this.cecefjUid = cecefjUid;
    }

    public long getCecefjContentEntryUid() {
        return cecefjContentEntryUid;
    }

    public void setCecefjContentEntryUid(long cecefjContentEntryUid) {
        this.cecefjContentEntryUid = cecefjContentEntryUid;
    }

    public long getCecefjContentEntryFileUid() {
        return cecefjContentEntryFileUid;
    }

    public void setCecefjContentEntryFileUid(long cecefjContentEntryFileUid) {
        this.cecefjContentEntryFileUid = cecefjContentEntryFileUid;
    }

    public long getCecefjLocalChangeSeqNum() {
        return cecefjLocalChangeSeqNum;
    }

    public void setCecefjLocalChangeSeqNum(long cecefjLocalChangeSeqNum) {
        this.cecefjLocalChangeSeqNum = cecefjLocalChangeSeqNum;
    }

    public long getCecefjMasterChangeSeqNum() {
        return cecefjMasterChangeSeqNum;
    }

    public void setCecefjMasterChangeSeqNum(long cecefjMasterChangeSeqNum) {
        this.cecefjMasterChangeSeqNum = cecefjMasterChangeSeqNum;
    }

    public int getCecefjLastChangedBy() {
        return cecefjLastChangedBy;
    }

    public void setCecefjLastChangedBy(int cecefjLastChangedBy) {
        this.cecefjLastChangedBy = cecefjLastChangedBy;
    }

    public long getCecefjContainerUid() {
        return cecefjContainerUid;
    }

    public void setCecefjContainerUid(long cecefjContainerUid) {
        this.cecefjContainerUid = cecefjContainerUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryContentEntryFileJoin fileJoin = (ContentEntryContentEntryFileJoin) o;

        if (cecefjUid != fileJoin.cecefjUid) return false;
        if (cecefjContentEntryUid != fileJoin.cecefjContentEntryUid) return false;
        if (cecefjContainerUid != fileJoin.cecefjContainerUid) return false;
        return cecefjContentEntryFileUid == fileJoin.cecefjContentEntryFileUid;
    }

    @Override
    public int hashCode() {
        int result = (int) (cecefjUid ^ (cecefjUid >>> 32));
        result = 31 * result + (int) (cecefjContentEntryUid ^ (cecefjContentEntryUid >>> 32));
        result = 31 * result + (int) (cecefjContainerUid ^ (cecefjContainerUid >>> 32));
        result = 31 * result + (int) (cecefjContentEntryFileUid ^ (cecefjContentEntryFileUid >>> 32));
        return result;
    }
}
