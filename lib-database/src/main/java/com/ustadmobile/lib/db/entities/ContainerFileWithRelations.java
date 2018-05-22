package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmRelation;

import java.util.List;

/**
 * Created by mike on 1/26/18.
 */

public class ContainerFileWithRelations extends ContainerFile{

    @UmRelation(parentColumn = "id", entityColumn = "containerFileId")
    private List<ContainerFileEntry> entries;

    public List<ContainerFileEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ContainerFileEntry> entries) {
        this.entries = entries;
    }
}
