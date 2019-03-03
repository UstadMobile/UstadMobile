package com.ustadmobile.lib.db.entities;

public class ClazzLogWithScheduleStartEndTimes extends ClazzLog {

    //Start time
    private long sceduleStartTime;

    //End time
    private long scheduleEndTime;

    public long getSceduleStartTime() {
        return sceduleStartTime;
    }

    public void setSceduleStartTime(long sceduleStartTime) {
        this.sceduleStartTime = sceduleStartTime;
    }

    public long getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(long scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }
}
