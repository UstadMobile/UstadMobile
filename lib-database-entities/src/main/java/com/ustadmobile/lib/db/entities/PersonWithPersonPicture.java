package com.ustadmobile.lib.db.entities;

public class PersonWithPersonPicture extends Person {
    private long personPictureUid;

    public long getPersonPictureUid() {
        return personPictureUid;
    }

    public void setPersonPictureUid(long personPictureUid) {
        this.personPictureUid = personPictureUid;
    }
}
