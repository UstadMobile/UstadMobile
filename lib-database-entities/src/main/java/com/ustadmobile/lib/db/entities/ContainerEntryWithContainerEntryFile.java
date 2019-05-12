package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ContainerEntryWithContainerEntryFile extends ContainerEntry {

    @UmEmbedded
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
