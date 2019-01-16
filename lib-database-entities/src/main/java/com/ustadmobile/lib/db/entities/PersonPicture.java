package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.PersonPicture.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class PersonPicture {

    public static final int TABLE_ID = 50;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personPictureUid;

    private long personPicturePersonUid;

    @UmSyncMasterChangeSeqNum
    private long personPictureMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long personPictureLocalCsn;

    @UmSyncLastChangedBy
    private int personPictureLastChangedBy;

    private int fileSize;

    private int picTimestamp;

    private String mimeType;


    public long getPersonPictureUid() {
        return personPictureUid;
    }

    public void setPersonPictureUid(long personPictureUid) {
        this.personPictureUid = personPictureUid;
    }

    public long getPersonPicturePersonUid() {
        return personPicturePersonUid;
    }

    public void setPersonPicturePersonUid(long personPicturePersonUid) {
        this.personPicturePersonUid = personPicturePersonUid;
    }

    public long getPersonPictureMasterCsn() {
        return personPictureMasterCsn;
    }

    public void setPersonPictureMasterCsn(long personPictureMasterCsn) {
        this.personPictureMasterCsn = personPictureMasterCsn;
    }

    public long getPersonPictureLocalCsn() {
        return personPictureLocalCsn;
    }

    public void setPersonPictureLocalCsn(long personPictureLocalCsn) {
        this.personPictureLocalCsn = personPictureLocalCsn;
    }

    public int getPersonPictureLastChangedBy() {
        return personPictureLastChangedBy;
    }

    public void setPersonPictureLastChangedBy(int personPictureLastChangedBy) {
        this.personPictureLastChangedBy = personPictureLastChangedBy;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPicTimestamp() {
        return picTimestamp;
    }

    public void setPicTimestamp(int picTimestamp) {
        this.picTimestamp = picTimestamp;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
