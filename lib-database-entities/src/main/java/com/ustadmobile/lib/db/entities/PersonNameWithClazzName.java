package com.ustadmobile.lib.db.entities;

public class PersonNameWithClazzName {

    long clazzMemberUid;
    long personUid;
    String firstNames;
    String lastName;
    int num;
    String clazzName;

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public long getClazzMemberUid() {
        return clazzMemberUid;
    }

    public void setClazzMemberUid(long clazzMemberUid) {
        this.clazzMemberUid = clazzMemberUid;
    }

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }



}
