package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1008)
public class WamdaSubject{

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long subjectUid;

    private String subjectName;

    private String subjectPoster;

    @UmSyncLocalChangeSeqNum
    private long wamdaSubjectLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wamdaSubjectMasterChangeSeqNum;

    public long getSubjectUid() {
        return subjectUid;
    }

    public void setSubjectUid(long subjectUid) {
        this.subjectUid = subjectUid;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectPoster() {
        return subjectPoster;
    }

    public void setSubjectPoster(String subjectPoster) {
        this.subjectPoster = subjectPoster;
    }

    public long getWamdaSubjectLocalChangeSeqNum() {
        return wamdaSubjectLocalChangeSeqNum;
    }

    public void setWamdaSubjectLocalChangeSeqNum(long wamdaSubjectLocalChangeSeqNum) {
        this.wamdaSubjectLocalChangeSeqNum = wamdaSubjectLocalChangeSeqNum;
    }

    public long getWamdaSubjectMasterChangeSeqNum() {
        return wamdaSubjectMasterChangeSeqNum;
    }

    public void setWamdaSubjectMasterChangeSeqNum(long wamdaSubjectMasterChangeSeqNum) {
        this.wamdaSubjectMasterChangeSeqNum = wamdaSubjectMasterChangeSeqNum;
    }
}
