package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 3/8/18.
 */

@UmEntity
public class Person implements SyncableEntity {

    public static final int GENDER_UNSET = 0;

    public static final int GENDER_FEMALE = 1;

    public static final int GENDER_MALE = 2;

    public static final int GENDER_OTHER = 4;

    @UmPrimaryKey(autoIncrement = true)
    private long personUid;

    private String username;

    private String passwordHash;

    private String firstNames;

    private String lastName;

    private String emailAddr;

    private String phoneNum;

    private int gender;

    private boolean active;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person person = (Person) o;

        if (personUid != person.personUid) return false;
        if (gender != person.gender) return false;
        if (active != person.active) return false;
        if (masterChangeSeqNum != person.masterChangeSeqNum) return false;
        if (localChangeSeqNum != person.localChangeSeqNum) return false;
        if (username != null ? !username.equals(person.username) : person.username != null)
            return false;
        if (passwordHash != null ? !passwordHash.equals(person.passwordHash) : person.passwordHash != null)
            return false;
        if (firstNames != null ? !firstNames.equals(person.firstNames) : person.firstNames != null)
            return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null)
            return false;
        if (emailAddr != null ? !emailAddr.equals(person.emailAddr) : person.emailAddr != null)
            return false;
        return phoneNum != null ? phoneNum.equals(person.phoneNum) : person.phoneNum == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (personUid ^ (personUid >>> 32));
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (passwordHash != null ? passwordHash.hashCode() : 0);
        result = 31 * result + (firstNames != null ? firstNames.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (emailAddr != null ? emailAddr.hashCode() : 0);
        result = 31 * result + (phoneNum != null ? phoneNum.hashCode() : 0);
        result = 31 * result + gender;
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (int) (masterChangeSeqNum ^ (masterChangeSeqNum >>> 32));
        result = 31 * result + (int) (localChangeSeqNum ^ (localChangeSeqNum >>> 32));
        return result;
    }
}
