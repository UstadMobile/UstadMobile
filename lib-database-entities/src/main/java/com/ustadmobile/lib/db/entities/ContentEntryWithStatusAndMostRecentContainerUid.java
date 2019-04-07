package com.ustadmobile.lib.db.entities;

public class ContentEntryWithStatusAndMostRecentContainerUid extends ContentEntryWithContentEntryStatus {

    private long mostRecentContainer;

    public long getMostRecentContainer() {
        return mostRecentContainer;
    }

    public void setMostRecentContainer(long mostRecentContainer) {
        this.mostRecentContainer = mostRecentContainer;
    }

}
