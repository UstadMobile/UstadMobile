package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaClazzWithSocialInfoClazzMember extends Clazz {

    private int numLikes;

    private int numStudents;

    private int numShares;

    @UmEmbedded
    private WamdaClazz wamdaClazz;

    @UmEmbedded
    private Person person;

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public int getNumShares() {
        return numShares;
    }

    public void setNumShares(int numShares) {
        this.numShares = numShares;
    }

    public WamdaClazz getWamdaClazz() {
        return wamdaClazz;
    }

    public void setWamdaClazz(WamdaClazz wamdaClazz) {
        this.wamdaClazz = wamdaClazz;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
