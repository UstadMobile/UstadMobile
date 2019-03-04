package com.ustadmobile.lib.contentscrapers.util;

public class SrtFormat {

    private String text;

    private long endTime;

    private long startTime;

    public SrtFormat(String text, long endTime, long startTime) {
        this.text = text;
        this.endTime = endTime;
        this.startTime = startTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


}
