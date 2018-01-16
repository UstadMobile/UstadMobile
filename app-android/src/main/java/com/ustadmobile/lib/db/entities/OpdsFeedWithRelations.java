package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsFeedWithRelations extends com.ustadmobile.lib.db.entities.OpdsFeed
        implements OpdsItemWithLinks{

    @Relation(parentColumn = "id", entityColumn = "feedId")
    private List<OpdsLink> links;

    public List<OpdsLink> getLinks() {
        return links;
    }

    public void setLinks(List<OpdsLink> links) {
        this.links = links;
    }

}
