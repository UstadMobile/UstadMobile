package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/26/18.
 */
@UmEntity
public class DownloadJobItem {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private int downloadJobId;

    private String opdsEntryUuid;

    private String entryId;

    private long updated;

    private int status;

    private int containerFileId;

    private long downloadLength;

    public DownloadJobItem() {

    }

    public DownloadJobItem(OpdsEntryWithRelations entry, DownloadJob job) {
        this.downloadJobId = job.getId();
        this.entryId = entry.getEntryId();
//        this.updated = entry.getUpdated();

//        TODO: determine the length of this download
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDownloadJobId() {
        return downloadJobId;
    }

    public void setDownloadJobId(int downloadJobId) {
        this.downloadJobId = downloadJobId;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getContainerFileId() {
        return containerFileId;
    }

    public void setContainerFileId(int containerFileId) {
        this.containerFileId = containerFileId;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }
}
