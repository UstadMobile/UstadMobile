package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.Person.TABLE_ID;

/**
 * Created by mike on 3/8/18.
 */

@UmEntity(tableId = TABLE_ID)
public class Person  {

    public static final int TABLE_ID = 9;

    public static final int GENDER_UNSET = 0;

    public static final int GENDER_FEMALE = 1;

    public static final int GENDER_MALE = 2;

    public static final int GENDER_OTHER = 4;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personUid;

    private String username;

    private String firstNames;

    private String lastName;

    private String emailAddr;

    private String phoneNum;

    private int gender;

    private boolean active;

    private boolean admin;

    @UmSyncMasterChangeSeqNum
    private long personMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long personLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int personLastChangedBy;

    private String fatherName;

    private String fatherNumber;

    private String motherName;

    private String motherNum;

    private long dateOfBirth;

    private String address;

    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherNumber() {
        return fatherNumber;
    }

    public void setFatherNumber(String fatherNumber) {
        this.fatherNumber = fatherNumber;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherNum() {
        return motherNum;
    }

    public void setMotherNum(String motherNum) {
        this.motherNum = motherNum;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Person() {

    }

    public Person(String username, String firstNames, String lastName){
        this.username = username;
        this.firstNames = firstNames;
        this.lastName = lastName;
    }

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

    public int getPersonLastChangedBy() {
        return personLastChangedBy;
    }

    public void setPersonLastChangedBy(int personLastChangedBy) {
        this.personLastChangedBy = personLastChangedBy;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
