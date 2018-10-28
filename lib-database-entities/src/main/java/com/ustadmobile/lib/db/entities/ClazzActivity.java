package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ClazzActivity {

    @UmPrimaryKey(autoIncrement = true)
    private long clazzActivityUid;

    private long clazzActivityClazzActivityChangeUid;

    private long clazzActivityDuration;

    private boolean clazzActivityGoodFeedback;

    private String clazzActivityNotes;

    private long clazzActivityLogDate;

    public long getClazzActivityUid() {
        return clazzActivityUid;
    }

    public void setClazzActivityUid(long clazzActivityUid) {
        this.clazzActivityUid = clazzActivityUid;
    }

    public long getClazzActivityClazzActivityChangeUid() {
        return clazzActivityClazzActivityChangeUid;
    }

    public void setClazzActivityClazzActivityChangeUid(long clazzActivityClazzActivityChangeUid) {
        this.clazzActivityClazzActivityChangeUid = clazzActivityClazzActivityChangeUid;
    }

    public long getClazzActivityDuration() {
        return clazzActivityDuration;
    }

    public void setClazzActivityDuration(long clazzActivityDuration) {
        this.clazzActivityDuration = clazzActivityDuration;
    }

    public boolean isClazzActivityGoodFeedback() {
        return clazzActivityGoodFeedback;
    }

    public void setClazzActivityGoodFeedback(boolean clazzActivityGoodFeedback) {
        this.clazzActivityGoodFeedback = clazzActivityGoodFeedback;
    }

    public String getClazzActivityNotes() {
        return clazzActivityNotes;
    }

    public void setClazzActivityNotes(String clazzActivityNotes) {
        this.clazzActivityNotes = clazzActivityNotes;
    }

    public long getClazzActivityLogDate() {
        return clazzActivityLogDate;
    }

    public void setClazzActivityLogDate(long clazzActivityLogDate) {
        this.clazzActivityLogDate = clazzActivityLogDate;
    }
}
