package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;


/**
 * Created by mike on 1/13/18.
 */
@UmEntity
public class OpdsEntry extends OpdsItem {

    private String feedId;

    private String entryId;

    private int feedIndex;

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getFeedIndex() {
        return feedIndex;
    }

    public void setFeedIndex(int feedIndex) {
        this.feedIndex = feedIndex;
    }
}