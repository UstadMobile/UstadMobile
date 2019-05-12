package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ScrapeQueueItem {

    public static final int ITEM_TYPE_INDEX = 1;

    public static final int ITEM_TYPE_SCRAPE = 2;

    @UmPrimaryKey(autoIncrement = true)
    private int sqiUid;

    private long sqiContentEntryParentUid;

    private String destDir;

    private String scrapeUrl;

    private int status;

    private int runId;

    private int itemType;

    private String contentType;

    private long timeAdded;

    private long timeStarted;

    private long timeFinished;

    public int getSqiUid() {
        return sqiUid;
    }

    public void setSqiUid(int sqiUid) {
        this.sqiUid = sqiUid;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSqiContentEntryParentUid() {
        return sqiContentEntryParentUid;
    }

    public void setSqiContentEntryParentUid(long sqiContentEntryParentUid) {
        this.sqiContentEntryParentUid = sqiContentEntryParentUid;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public String getScrapeUrl() {
        return scrapeUrl;
    }

    public void setScrapeUrl(String scrapeUrl) {
        this.scrapeUrl = scrapeUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(long timeFinished) {
        this.timeFinished = timeFinished;
    }
}
