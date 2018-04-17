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

    @UmIndexField
    private String containerEntryId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerFileEntry)) return false;

        ContainerFileEntry that = (ContainerFileEntry) o;

        if (containerFileEntryId != that.containerFileEntryId) return false;
        if (containerFileId != that.containerFileId) return false;
        if (containerEntryUpdated != that.containerEntryUpdated) return false;
        if (containerEntryId != null ? !containerEntryId.equals(that.containerEntryId) : that.containerEntryId != null)
            return false;
        return opdsEntryUuid != null ? opdsEntryUuid.equals(that.opdsEntryUuid) : that.opdsEntryUuid == null;
    }

    @Override
    public int hashCode() {
        int result = containerFileEntryId;
        result = 31 * result + containerFileId;
        result = 31 * result + (containerEntryId != null ? containerEntryId.hashCode() : 0);
        result = 31 * result + (int) (containerEntryUpdated ^ (containerEntryUpdated >>> 32));
        result = 31 * result + (opdsEntryUuid != null ? opdsEntryUuid.hashCode() : 0);
        return result;
    }
}
