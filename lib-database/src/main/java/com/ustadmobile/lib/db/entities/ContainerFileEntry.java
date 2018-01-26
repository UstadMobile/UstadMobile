package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/25/18.
 */

public class ContainerFileEntry {

    @UmPrimaryKey
    private int id;

    private int containerFileId;

    String entryId;

    private long updated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getContainerFileId() {
        return containerFileId;
    }

    public void setContainerFileId(int containerFileId) {
        this.containerFileId = containerFileId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
