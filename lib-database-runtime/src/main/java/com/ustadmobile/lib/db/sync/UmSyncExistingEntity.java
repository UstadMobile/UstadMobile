package com.ustadmobile.lib.db.sync;

public class UmSyncExistingEntity {

    private long primaryKey;

    private boolean userCanUpdate;

    public UmSyncExistingEntity(long primaryKey, boolean userCanUpdate) {
        this.primaryKey = primaryKey;
        this.userCanUpdate = userCanUpdate;
    }

    public UmSyncExistingEntity() {

    }

    public long getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(long primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isUserCanUpdate() {
        return userCanUpdate;
    }

    public void setUserCanUpdate(boolean userCanUpdate) {
        this.userCanUpdate = userCanUpdate;
    }
}
