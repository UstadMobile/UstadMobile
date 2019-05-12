package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContentEntryWithContentEntryStatus extends ContentEntry {

    @UmEmbedded
    private ContentEntryStatus contentEntryStatus;

    public ContentEntryStatus getContentEntryStatus() {
        return contentEntryStatus;
    }

    public void setContentEntryStatus(ContentEntryStatus contentEntryStatus) {
        this.contentEntryStatus = contentEntryStatus;
    }
}
