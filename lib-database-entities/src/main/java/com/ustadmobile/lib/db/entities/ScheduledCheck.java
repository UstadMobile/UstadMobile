package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity(indices = {@UmIndex(name = "clazzUid_type_index", value = {"scClazzLogUid", "checkType"})})
public class ScheduledCheck {

    /**
     * Generate a FeedEntry for the teacher if attendance has not yet been recorded (generated at
     * the time class is due to start)
     */
    public static final int TYPE_RECORD_ATTENDANCE_REMINDER = 1;

    public static final int TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER = 2;

    public static final String PARAM_CLAZZ_UID = "clazzuid";

    public static final String PARAM_CLAZZ_LOG_UID = "clazzloguid";

    @UmPrimaryKey(autoIncrement = true)
    private long scheduledCheckId;

    private long checkTime;

    private int checkType;

    private String checkUuid;

    private String checkParameters;

    private long scClazzLogUid;

    public ScheduledCheck() {
        
    }

    public ScheduledCheck(long checkTime, int checkType, String checkParameters) {
        this.checkTime = checkTime;
        this.checkType = checkType;
        this.checkParameters = checkParameters;
    }

    public ScheduledCheck(long checkTime, int checkType, long clazzLogUid) {
        this.checkTime = checkTime;
        this.checkType = checkType;
        this.scClazzLogUid = clazzLogUid;
    }

    public long getScheduledCheckId() {
        return scheduledCheckId;
    }

    public void setScheduledCheckId(long scheduledCheckId) {
        this.scheduledCheckId = scheduledCheckId;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public int getCheckType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public String getCheckParameters() {
        return checkParameters;
    }

    public void setCheckParameters(String checkParameters) {
        this.checkParameters = checkParameters;
    }

    public String getCheckUuid() {
        return checkUuid;
    }

    public void setCheckUuid(String checkUuid) {
        this.checkUuid = checkUuid;
    }

    public long getScClazzLogUid() {
        return scClazzLogUid;
    }

    public void setScClazzLogUid(long scClazzLogUid) {
        this.scClazzLogUid = scClazzLogUid;
    }
}
