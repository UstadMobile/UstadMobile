package com.ustadmobile.lib.db.entities;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsFeed extends OpdsItem {

    private String feedId;

    private String title;

    private String lang;

    private String updated;



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
