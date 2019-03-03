package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContentEntryWithFileJoinStatus extends ContentEntryContentEntryFileJoin {

    @UmEmbedded
    private ContentEntryFile contentEntryFile;

    @UmEmbedded
    private ContentEntryFileStatus contentEntryFileStatus;

    public ContentEntryFile getContentEntryFile() {
        return contentEntryFile;
    }

    public void setContentEntryFile(ContentEntryFile contentEntryFile) {
        this.contentEntryFile = contentEntryFile;
    }

    public ContentEntryFileStatus getContentEntryFileStatus() {
        return contentEntryFileStatus;
    }

    public void setContentEntryFileStatus(ContentEntryFileStatus contentEntryFileStatus) {
        this.contentEntryFileStatus = contentEntryFileStatus;
    }
}
