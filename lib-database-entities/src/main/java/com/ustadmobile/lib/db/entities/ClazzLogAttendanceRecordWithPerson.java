package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ClazzLogAttendanceRecordWithPerson extends ClazzLogAttendanceRecord {

    @UmEmbedded
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
