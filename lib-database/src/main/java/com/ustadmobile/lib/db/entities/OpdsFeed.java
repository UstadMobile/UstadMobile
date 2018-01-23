package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;

/**
 * Created by mike on 1/13/18.
 */
@UmEntity
public class OpdsFeed extends OpdsItem {

    private String feedId;

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }


}
