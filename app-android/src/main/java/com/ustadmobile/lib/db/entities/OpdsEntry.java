package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

/**
 * Created by mike on 1/13/18.
 */

@Entity
public class OpdsEntry extends com.ustadmobile.lib.db.entities.OpdsItem {

    @ColumnInfo(name = "feed_id")
    private int feedId;

    @ColumnInfo(name = "entry_id")
    private String entryId;

    @ColumnInfo(name = "feed_index")
    private int feedIndex;

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
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