package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/25/18.
 */
@UmEntity
public class ContainerFileEntry {

    @UmPrimaryKey(autoIncrement = true)
    private int containerFileEntryId;

    @UmIndexField
    private int containerFileId;

    String containerEntryId;

    private long containerEntryUpdated;

    @UmIndexField
    private String opdsEntryUuid;

    public int getContainerFileEntryId() {
        return containerFileEntryId;
    }

    public void setContainerFileEntryId(int containerFileEntryId) {
        this.containerFileEntryId = containerFileEntryId;
    }

    public int getContainerFileId() {
        return containerFileId;
    }

    public void setContainerFileId(int containerFileId) {
        this.containerFileId = containerFileId;
    }

    public String getContainerEntryId() {
        return containerEntryId;
    }

    public void setContainerEntryId(String containerEntryId) {
        this.containerEntryId = containerEntryId;
    }

    public long getContainerEntryUpdated() {
        return containerEntryUpdated;
    }

    public void setContainerEntryUpdated(long containerEntryUpdated) {
        this.containerEntryUpdated = containerEntryUpdated;
    }

    public String getOpdsEntryUuid() {
        return opdsEntryUuid;
    }

    public void setOpdsEntryUuid(String opdsEntryUuid) {
        this.opdsEntryUuid = opdsEntryUuid;
    }
}
