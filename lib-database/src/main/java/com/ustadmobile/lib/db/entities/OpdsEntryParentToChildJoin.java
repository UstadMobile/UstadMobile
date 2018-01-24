package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;

/**
 * Created by mike on 1/23/18.
 */
@UmEntity(
        primaryKeys = {"parentEntry", "childEntry"},
        indices = {@UmIndex(name = "parent_index_unique", value = {"parentEntry", "childIndex"}, unique = true)}
)
public class OpdsEntryParentToChildJoin {

    private String parentEntry;

    private String childEntry;

    private int childIndex;


    public OpdsEntryParentToChildJoin(String parentEntry, String childEntry, int childIndex) {
        this.parentEntry = parentEntry;
        this.childEntry = childEntry;
        this.childIndex = childIndex;
    }

    public String getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(String parentEntry) {
        this.parentEntry = parentEntry;
    }

    public String getChildEntry() {
        return childEntry;
    }

    public void setChildEntry(String childEntry) {
        this.childEntry = childEntry;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }
}
