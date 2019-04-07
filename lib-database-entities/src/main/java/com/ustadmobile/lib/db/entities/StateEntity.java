package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.StateEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class StateEntity {

    public static final int TABLE_ID = 70;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long stateUid;

    private String stateId;

    private long agentUid;

    private String activityId;

    private String registration;

    private boolean isactive;

    private long timestamp;

    public StateEntity(String activityId, long agentUid, String registration, String stateId, boolean isActive, long timestamp) {
        this.activityId = activityId;
        this.agentUid = agentUid;
        this.registration = registration;
        this.isactive = isActive;
        this.stateId = stateId;
        this.timestamp = timestamp;
    }

    public StateEntity() {

    }

    public long getStateUid() {
        return stateUid;
    }

    @UmSyncMasterChangeSeqNum
    private long stateMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long stateLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int stateLastChangedBy;

    public void setStateUid(long stateUid) {
        this.stateUid = stateUid;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityUid) {
        this.activityId = activityUid;
    }

    public long getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(long agentUid) {
        this.agentUid = agentUid;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    public long getStateMasterChangeSeqNum() {
        return stateMasterChangeSeqNum;
    }

    public void setStateMasterChangeSeqNum(long stateMasterChangeSeqNum) {
        this.stateMasterChangeSeqNum = stateMasterChangeSeqNum;
    }

    public long getStateLocalChangeSeqNum() {
        return stateLocalChangeSeqNum;
    }

    public void setStateLocalChangeSeqNum(long stateLocalChangeSeqNum) {
        this.stateLocalChangeSeqNum = stateLocalChangeSeqNum;
    }

    public int getStateLastChangedBy() {
        return stateLastChangedBy;
    }

    public void setStateLastChangedBy(int stateLastChangedBy) {
        this.stateLastChangedBy = stateLastChangedBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateEntity that = (StateEntity) o;

        if (stateUid != that.stateUid) return false;
        if (agentUid != that.agentUid) return false;
        if (isactive != that.isactive) return false;
        if (stateId != null ? !stateId.equals(that.stateId) : that.stateId != null) return false;
        if (activityId != null ? !activityId.equals(that.activityId) : that.activityId != null)
            return false;
        return registration != null ? registration.equals(that.registration) : that.registration == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (stateUid ^ (stateUid >>> 32));
        result = 31 * result + (stateId != null ? stateId.hashCode() : 0);
        result = 31 * result + (int) (agentUid ^ (agentUid >>> 32));
        result = 31 * result + (activityId != null ? activityId.hashCode() : 0);
        result = 31 * result + (registration != null ? registration.hashCode() : 0);
        result = 31 * result + (isactive ? 1 : 0);
        return result;
    }
}
