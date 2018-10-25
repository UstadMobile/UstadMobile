package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * POJO representing Person and ClazzMember
 */
public class ClazzMemberWithPerson extends ClazzMember {

    @UmEmbedded
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
