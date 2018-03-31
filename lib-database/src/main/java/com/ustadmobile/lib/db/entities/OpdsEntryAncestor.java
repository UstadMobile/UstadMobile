package com.ustadmobile.lib.db.entities;

/**
 * Created by mike on 3/24/18.
 */

public class OpdsEntryAncestor {

    private String entryId;

    private String descendantId;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getDescendantId() {
        return descendantId;
    }

    public void setDescendantId(String descendantId) {
        this.descendantId = descendantId;
    }
}
