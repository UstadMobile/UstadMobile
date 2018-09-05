package com.ustadmobile.lib.db.entities;

public interface SyncableEntity {

    long getMasterChangeSeqNum();

    void setMasterChangeSeqNum(long masterChangeSeqNum);

    long getLocalChangeSeqNum();

    void setLocalChangeSeqNum(long localChangeSeqNum);

}
