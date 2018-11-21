package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class WamdaSubject{

    @UmPrimaryKey(autoIncrement = true)
    private long subjectUid;

    private String subjectName;

    private String subjectPoster;

    public long getSubjectUid() {
        return subjectUid;
    }

    public void setSubjectUid(long subjectUid) {
        this.subjectUid = subjectUid;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectPoster() {
        return subjectPoster;
    }

    public void setSubjectPoster(String subjectPoster) {
        this.subjectPoster = subjectPoster;
    }
}
