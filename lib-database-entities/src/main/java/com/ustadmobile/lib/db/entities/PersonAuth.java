package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * This is a 1:1 relationship with Person. It avoids synchronizing login credentials with any other
 * devices in cases where another user has permission to view someone else's profile.
 *
 * There is no foreign key field, personAuthUid simply equals personUid.
 */
@UmEntity(tableId = 30)
public class PersonAuth {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personAuthUid;

    private String passwordHash;

    @UmSyncLocalChangeSeqNum
    private long personAuthLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long personAuthMasterChangeSeqNum;

    public PersonAuth() {

    }

    public PersonAuth(long personAuthUid, String passwordHash) {
        this.personAuthUid = personAuthUid;
        this.passwordHash = passwordHash;
    }

    public long getPersonAuthUid() {
        return personAuthUid;
    }

    public void setPersonAuthUid(long personAuthUid) {
        this.personAuthUid = personAuthUid;
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