package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Created by mike on 3/8/18.
 */

@UmEntity(tableId = 9)
public class Person  {

    public static final int GENDER_UNSET = 0;

    public static final int GENDER_FEMALE = 1;

    public static final int GENDER_MALE = 2;

    public static final int GENDER_OTHER = 4;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personUid;

    private String username;

    private String passwordHash;

    private String firstNames;

    private String lastName;

    private String emailAddr;

    private String phoneNum;

    private int gender;

    private boolean active;

    @UmSyncMasterChangeSeqNum
    private long personMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long personLocalChangeSeqNum;

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public long getPersonMasterChangeSeqNum() {
        return personMasterChangeSeqNum;
    }

    public void setPersonMasterChangeSeqNum(long personMasterChangeSeqNum) {
        this.personMasterChangeSeqNum = personMasterChangeSeqNum;
    }

    public long getPersonLocalChangeSeqNum() {
        return personLocalChangeSeqNum;
    }

    public void setPersonLocalChangeSeqNum(long personLocalChangeSeqNum) {
        this.personLocalChangeSeqNum = personLocalChangeSeqNum;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
