package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Entity;

/**
 * Created by mike on 1/13/18.
 */
@Entity
public class OpdsFeed extends com.ustadmobile.lib.db.entities.OpdsItem {

    private String feedId;

    private String lang;

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

}
