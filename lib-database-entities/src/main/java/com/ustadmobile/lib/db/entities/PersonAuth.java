package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 30)
public class PersonAuth {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personAuthUid;

    private long personAuthPersonUid;

    private String passwordHash;

    @UmSyncLocalChangeSeqNum
    private long personAuthLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long personAuthMasterChangeSeqNum;

    public long getPersonAuthUid() {
        return personAuthUid;
    }

    public void setPersonAuthUid(long personAuthUid) {
        this.personAuthUid = personAuthUid;
    }

    public long getPersonAuthPersonUid() {
        return personAuthPersonUid;
    }

    public void setPersonAuthPersonUid(long personAuthPersonUid) {
        this.personAuthPersonUid = personAuthPersonUid;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public long getPersonAuthLocalChangeSeqNum() {
        return personAuthLocalChangeSeqNum;
    }

    public void setPersonAuthLocalChangeSeqNum(long personAuthLocalChangeSeqNum) {
        this.personAuthLocalChangeSeqNum = personAuthLocalChangeSeqNum;
    }

    public long getPersonAuthMasterChangeSeqNum() {
        return personAuthMasterChangeSeqNum;
    }

    public void setPersonAuthMasterChangeSeqNum(long personAuthMasterChangeSeqNum) {
        this.personAuthMasterChangeSeqNum = personAuthMasterChangeSeqNum;
    }
}