package com.ustadmobile.lib.db.sync;

import java.util.HashMap;
import java.util.List;

public class SyncResponse<T> {

    private HashMap<Long, Long> assignedMasterSequenceIds;

    private List<T> remoteChangedEntities;

    private long syncedUpToMasterChangeSeqNum;

    public SyncResponse() {
        assignedMasterSequenceIds = new HashMap<>();
    }

    public HashMap<Long, Long> getAssignedMasterSequenceIds() {
        return assignedMasterSequenceIds;
    }

    public void setAssignedMasterSequenceIds(HashMap<Long, Long> assignedMasterSequenceIds) {
        this.assignedMasterSequenceIds = assignedMasterSequenceIds;
    }

    public List<T> getRemoteChangedEntities() {
        return remoteChangedEntities;
    }

    public void setRemoteChangedEntities(List<T> remoteChangedEntities) {
        this.remoteChangedEntities = remoteChangedEntities;
    }

    public long getSyncedUpToMasterChangeSeqNum() {
        return syncedUpToMasterChangeSeqNum;
    }

    public void setSyncedUpToMasterChangeSeqNum(long syncedUpToMasterChangeSeqNum) {
        this.syncedUpToMasterChangeSeqNum = syncedUpToMasterChangeSeqNum;
    }
}
