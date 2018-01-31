package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@UmEntity
public class EntryStatusResponse {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private String entryId;

    private long updated;

    private long responseTime;

    private int nodeId;

    private boolean available;

    public EntryStatusResponse(String entryId, int nodeId, long responseTime, long updated,
                               boolean available) {
        this.entryId = entryId;
        this.nodeId = nodeId;
        this.updated = updated;
        this.responseTime = responseTime;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
