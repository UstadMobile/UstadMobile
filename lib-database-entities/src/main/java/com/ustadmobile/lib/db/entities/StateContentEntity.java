package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.StateContentEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class StateContentEntity {

    public static final int TABLE_ID = 72;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long stateContentUid;

    private long stateContentStateUid;

    private String stateContentKey;

    private String stateContentValue;

    private boolean isactive;

    @UmSyncMasterChangeSeqNum
    private long stateContentMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long stateContentLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int stateContentLastChangedBy;

    public StateContentEntity(String key, long stateUid, String valueOf, boolean isActive) {
        this.stateContentKey = key;
        this.stateContentValue = valueOf;
        this.isactive = isActive;
        this.stateContentStateUid = stateUid;
    }

    public StateContentEntity(){

    }

    public long getStateContentUid() {
        return stateContentUid;
    }

    public void setStateContentUid(long stateContentUid) {
        this.stateContentUid = stateContentUid;
    }

    public long getStateContentStateUid() {
        return stateContentStateUid;
    }

    public void setStateContentStateUid(long stateContentStateUid) {
        this.stateContentStateUid = stateContentStateUid;
    }

    public String getStateContentKey() {
        return stateContentKey;
    }

    public void setStateContentKey(String stateContentKey) {
        this.stateContentKey = stateContentKey;
    }

    public String getStateContentValue() {
        return stateContentValue;
    }

    public void setStateContentValue(String stateContentValue) {
        this.stateContentValue = stateContentValue;
    }

    public long getStateContentMasterChangeSeqNum() {
        return stateContentMasterChangeSeqNum;
    }

    public void setStateContentMasterChangeSeqNum(long stateContentMasterChangeSeqNum) {
        this.stateContentMasterChangeSeqNum = stateContentMasterChangeSeqNum;
    }

    public long getStateContentLocalChangeSeqNum() {
        return stateContentLocalChangeSeqNum;
    }

    public void setStateContentLocalChangeSeqNum(long stateContentLocalChangeSeqNum) {
        this.stateContentLocalChangeSeqNum = stateContentLocalChangeSeqNum;
    }

    public int getStateContentLastChangedBy() {
        return stateContentLastChangedBy;
    }

    public void setStateContentLastChangedBy(int stateContentLastChangedBy) {
        this.stateContentLastChangedBy = stateContentLastChangedBy;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateContentEntity that = (StateContentEntity) o;

        if (stateContentUid != that.stateContentUid) return false;
        if (stateContentStateUid != that.stateContentStateUid) return false;
        if (isactive != that.isactive) return false;
        if (stateContentKey != null ? !stateContentKey.equals(that.stateContentKey) : that.stateContentKey != null)
            return false;
        return stateContentValue != null ? stateContentValue.equals(that.stateContentValue) : that.stateContentValue == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (stateContentUid ^ (stateContentUid >>> 32));
        result = 31 * result + (int) (stateContentStateUid ^ (stateContentStateUid >>> 32));
        result = 31 * result + (stateContentKey != null ? stateContentKey.hashCode() : 0);
        result = 31 * result + (stateContentValue != null ? stateContentValue.hashCode() : 0);
        result = 31 * result + (isactive ? 1 : 0);
        return result;
    }
}
