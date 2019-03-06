package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@UmEntity(indices = {@UmIndex(name="containerUid_nodeId_unique", unique = true,
        value = {"erContainerUid", "erNodeId"})})
public class EntryStatusResponse {

    @UmPrimaryKey(autoIncrement = true)
    private int erId;

    private long erContainerUid;

    private long responseTime;

    private long erNodeId;

    private boolean available;

    public EntryStatusResponse(long erContainerUid, long responseTime, long erNodeId,
                               boolean available) {
        this.erContainerUid = erContainerUid;
        this.responseTime = responseTime;
        this.erNodeId = erNodeId;
        this.available = available;
    }


    public EntryStatusResponse() { }

    public int getErId() {
        return erId;
    }

    public void setErId(int erId) {
        this.erId = erId;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getErNodeId() {
        return erNodeId;
    }

    public void setErNodeId(long erNodeId) {
        this.erNodeId = erNodeId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public long getErContainerUid() {
        return erContainerUid;
    }

    public void setErContainerUid(long erContainerUid) {
        this.erContainerUid = erContainerUid;
    }
}
