package com.ustadmobile.lib.db.entities;

public class ClazzLogWithScheduleStartEndTimes extends ClazzLog {

    //Start time
    private long sceduleStartTime;

    //End time
    private long scheduleEndTime;

    //Frequency
    private int scheduleFrequency;

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

    public int getScheduleFrequency() {
        return scheduleFrequency;
    }

    public void setScheduleFrequency(int scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
    }
}
