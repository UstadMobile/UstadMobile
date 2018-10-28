package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ClazzActivityChange {

    @UmPrimaryKey(autoIncrement = true)
    private long clazzActivityChangeUid;

    private String clazzActivityChangeTitle;

    private String clazzActivityDesc;

    public long getClazzActivityChangeUid() {
        return clazzActivityChangeUid;
    }

    public void setClazzActivityChangeUid(long clazzActivityChangeUid) {
        this.clazzActivityChangeUid = clazzActivityChangeUid;
    }

    public String getClazzActivityChangeTitle() {
        return clazzActivityChangeTitle;
    }

    public void setClazzActivityChangeTitle(String clazzActivityTitle) {
        this.clazzActivityChangeTitle = clazzActivityTitle;
    }

    public String getClazzActivityDesc() {
        return clazzActivityDesc;
    }

    public void setClazzActivityDesc(String clazzActivityDesc) {
        this.clazzActivityDesc = clazzActivityDesc;
    }
}
