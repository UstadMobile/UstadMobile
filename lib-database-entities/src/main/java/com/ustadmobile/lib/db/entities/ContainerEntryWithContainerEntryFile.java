package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Embedded;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContainerEntryWithContainerEntryFile extends ContainerEntry {

    @UmEmbedded
    @Embedded
    private ContainerEntryFile containerEntryFile;

    public ContainerEntryWithContainerEntryFile(String cePath, Container container, ContainerEntryFile entryFile){
        super(cePath, container, entryFile);
        this.containerEntryFile = entryFile;
    }

    public ContainerEntryWithContainerEntryFile() {

    }

    public ContainerEntryFile getContainerEntryFile() {
        return containerEntryFile;
    }

    public void setContainerEntryFile(ContainerEntryFile containerEntryFile) {
        this.containerEntryFile = containerEntryFile;
    }
}
