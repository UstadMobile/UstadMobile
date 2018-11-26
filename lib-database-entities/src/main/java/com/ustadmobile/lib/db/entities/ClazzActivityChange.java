package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 12)
public class ClazzActivityChange{

    public static final int UOM_FREQUENCY = 1;
    public static final int UOM_DURATION = 2;
    public static final int UOM_BINARY = 3;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long clazzActivityChangeUid;

    private String clazzActivityChangeTitle;

    private String clazzActivityDesc;

    private int clazzActivityUnitOfMeasure;

    @UmSyncMasterChangeSeqNum
    private long clazzActivityChangeMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzActivityChangeLocalChangeSeqNum;

    public int getClazzActivityUnitOfMeasure() {
        return clazzActivityUnitOfMeasure;
    }

    public void setClazzActivityUnitOfMeasure(int clazzActivityUnitOfMeasure) {
        this.clazzActivityUnitOfMeasure = clazzActivityUnitOfMeasure;
    }

    public long getClazzActivityChangeUid() {
        return clazzActivityChangeUid;
    }

    public void setClazzActivityChangeUid(long clazzActivityChangeUid) {
        this.clazzActivityChangeUid = clazzActivityChangeUid;
    }

    public String getClazzActivityChangeTitle() {
        return clazzActivityChangeTitle;
    }

    public void setClazzActivityChangeTitle(String clazzActivityTitle) {
        this.clazzActivityChangeTitle = clazzActivityTitle;
    }

    public String getClazzActivityDesc() {
        return clazzActivityDesc;
    }

    public void setClazzActivityDesc(String clazzActivityDesc) {
        this.clazzActivityDesc = clazzActivityDesc;
    }

    public long getClazzActivityChangeMasterChangeSeqNum() {
        return clazzActivityChangeMasterChangeSeqNum;
    }

    public void setClazzActivityChangeMasterChangeSeqNum(long clazzActivityChangeMasterChangeSeqNum) {
        this.clazzActivityChangeMasterChangeSeqNum = clazzActivityChangeMasterChangeSeqNum;
    }

    public long getClazzActivityChangeLocalChangeSeqNum() {
        return clazzActivityChangeLocalChangeSeqNum;
    }

    public void setClazzActivityChangeLocalChangeSeqNum(long clazzActivityChangeLocalChangeSeqNum) {
        this.clazzActivityChangeLocalChangeSeqNum = clazzActivityChangeLocalChangeSeqNum;
    }
}
