package com.ustadmobile.lib.db.entities;

public class DailyActivityNumbers {

    private long clazzUid;
    private int good;
    private int bad;
    private long dayDate;

    public long getClazzUid() {
        return clazzUid;
    }

    public void setClazzUid(long clazzUid) {
        this.clazzUid = clazzUid;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getBad() {
        return bad;
    }

    public void setBad(int bad) {
        this.bad = bad;
    }

    public long getDayDate() {
        return dayDate;
    }

    public void setDayDate(long dayDate) {
        this.dayDate = dayDate;
    }
}
