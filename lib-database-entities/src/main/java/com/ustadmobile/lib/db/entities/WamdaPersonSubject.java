package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 1006)
public class WamdaPersonSubject {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long subjectPersonUid;

    private long subjectUid;

    private long personUid;

    @UmSyncLocalChangeSeqNum
    private long wpsLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long wpsMasterChangeSeqNum;

    public long getSubjectPersonUid() {
        return subjectPersonUid;
    }

    public void setSubjectPersonUid(long subjectPersonUid) {
        this.subjectPersonUid = subjectPersonUid;
    }

    public long getSubjectUid() {
        return subjectUid;
    }

    public void setSubjectUid(long subjectUid) {
        this.subjectUid = subjectUid;
    }

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }

    public long getWpsLocalChangeSeqNum() {
        return wpsLocalChangeSeqNum;
    }

    public void setWpsLocalChangeSeqNum(long wpsLocalChangeSeqNum) {
        this.wpsLocalChangeSeqNum = wpsLocalChangeSeqNum;
    }

    public long getWpsMasterChangeSeqNum() {
        return wpsMasterChangeSeqNum;
    }

    public void setWpsMasterChangeSeqNum(long wpsMasterChangeSeqNum) {
        this.wpsMasterChangeSeqNum = wpsMasterChangeSeqNum;
    }
}
