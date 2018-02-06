package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Created by mike on 2/5/18.
 */

public class ContainerFileEntryWithContainerFile extends ContainerFileEntry{

    @UmEmbedded
    private ContainerFile containerFile;

    public ContainerFile getContainerFile() {
        return containerFile;
    }

    public void setContainerFile(ContainerFile containerFile) {
        this.containerFile = containerFile;
    }
}
