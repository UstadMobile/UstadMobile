package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmRelation;

import java.util.List;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsFeedWithRelations extends OpdsFeed implements OpdsItemWithLinks{

    private List<OpdsLink> links;

    public List<OpdsLink> getLinks() {
        return links;
    }

    @UmRelation(parentColumn = "id", entityColumn = "feedId")
    public void setLinks(List<OpdsLink> links) {
        this.links = links;
    }
}
