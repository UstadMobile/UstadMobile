package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsEntryWithRelations extends OpdsEntry implements OpdsItemWithLinks {

    @Relation(parentColumn = "id", entityColumn = "entryId")
    private List<OpdsLink> links;

    public List<OpdsLink> getLinks() {
        return links;
    }

    public void setLinks(List<OpdsLink> links) {
        this.links = links;
    }

    public OpdsLink getThumbnail(boolean imgFallback) {
        return getThumbnailLink(links, imgFallback);
    }

}
