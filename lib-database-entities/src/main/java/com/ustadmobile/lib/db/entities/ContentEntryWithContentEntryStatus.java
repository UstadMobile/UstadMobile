package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Embedded;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContentEntryWithContentEntryStatus extends ContentEntry {

    @UmEmbedded
    @Embedded
    private ContentEntryStatus contentEntryStatus;

    public ContentEntryStatus getContentEntryStatus() {
        return contentEntryStatus;
    }

    public void setContentEntryStatus(ContentEntryStatus contentEntryStatus) {
        this.contentEntryStatus = contentEntryStatus;
    }
}
