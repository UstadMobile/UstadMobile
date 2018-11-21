package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaPersonSubject {

    @UmPrimaryKey(autoIncrement = true)
    private long subjectPersonUid;

    private long subjectUid;

    private long personUid;

    public long getSubjectPersonUid() {
        return subjectPersonUid;
    }

    public void setSubjectPersonUid(long subjectPersonUid) {
        this.subjectPersonUid = subjectPersonUid;
    }

    public long getSubjectUid() {
        return subjectUid;
    }

    public void setSubjectUid(long subjectUid) {
        this.subjectUid = subjectUid;
    }

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }
}
