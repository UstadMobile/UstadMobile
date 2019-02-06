package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContentEntryFileWithStatus extends ContentEntryFile{

    @UmEmbedded
    private ContentEntryFileStatus entryStatus;

    public ContentEntryFileStatus getEntryStatus() {
        return entryStatus;
    }

    public void setEntryStatus(ContentEntryFileStatus entryStatus) {
        this.entryStatus = entryStatus;
    }
}
