package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class ContextXObjectStatementJoin {

    public static final int TABLE_ID = 66;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long contextXObjectStatementJoinUid;

    private int contextActivityFlag;

    private long contextStatementUid;

    private long contextXObjectUid;

    @UmSyncMasterChangeSeqNum
    private long verbMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long verbLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int verbLastChangedBy;

    public long getContextXObjectStatementJoinUid() {
        return contextXObjectStatementJoinUid;
    }

    public void setContextXObjectStatementJoinUid(long contextXObjectStatementJoinUid) {
        this.contextXObjectStatementJoinUid = contextXObjectStatementJoinUid;
    }

    public int getContextActivityFlag() {
        return contextActivityFlag;
    }

    public void setContextActivityFlag(int contextActivityFlag) {
        this.contextActivityFlag = contextActivityFlag;
    }

    public long getContextStatementUid() {
        return contextStatementUid;
    }

    public void setContextStatementUid(long contextStatementUid) {
        this.contextStatementUid = contextStatementUid;
    }

    public long getContextXObjectUid() {
        return contextXObjectUid;
    }

    public void setContextXObjectUid(long contextXObjectUid) {
        this.contextXObjectUid = contextXObjectUid;
    }

    public long getVerbMasterChangeSeqNum() {
        return verbMasterChangeSeqNum;
    }

    public void setVerbMasterChangeSeqNum(long verbMasterChangeSeqNum) {
        this.verbMasterChangeSeqNum = verbMasterChangeSeqNum;
    }

    public long getVerbLocalChangeSeqNum() {
        return verbLocalChangeSeqNum;
    }

    public void setVerbLocalChangeSeqNum(long verbLocalChangeSeqNum) {
        this.verbLocalChangeSeqNum = verbLocalChangeSeqNum;
    }

    public int getVerbLastChangedBy() {
        return verbLastChangedBy;
    }

    public void setVerbLastChangedBy(int verbLastChangedBy) {
        this.verbLastChangedBy = verbLastChangedBy;
    }
}
