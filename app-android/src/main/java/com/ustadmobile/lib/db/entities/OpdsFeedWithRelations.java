package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsFeedWithRelations extends com.ustadmobile.lib.db.entities.OpdsFeed {

    @Relation(parentColumn = "id", entityColumn = "feed_id", entity = OpdsEntry.class)
    private List<OpdsEntryWithRelations> entries;

    public List<OpdsEntryWithRelations> getEntries() {
        return entries;
    }

    public void setEntries(List<OpdsEntryWithRelations> entries) {
        this.entries = entries;
    }
}
