package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.AgentEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class AgentEntity {

    public static final int TABLE_ID = 68;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long agentUid;

    private String agentMbox;

    private String agentMbox_sha1sum;

    private String agentOpenid;

    private String agentAccountName;

    @UmSyncMasterChangeSeqNum
    private long statementMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long statementLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int statementLastChangedBy;

    public long getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(long agentUid) {
        this.agentUid = agentUid;
    }

    public String getAgentMbox() {
        return agentMbox;
    }

    public void setAgentMbox(String agentMbox) {
        this.agentMbox = agentMbox;
    }

    public String getAgentMbox_sha1sum() {
        return agentMbox_sha1sum;
    }

    public void setAgentMbox_sha1sum(String agentMbox_sha1sum) {
        this.agentMbox_sha1sum = agentMbox_sha1sum;
    }

    public String getAgentOpenid() {
        return agentOpenid;
    }

    public void setAgentOpenid(String agentOpenid) {
        this.agentOpenid = agentOpenid;
    }

    public String getAgentAccountName() {
        return agentAccountName;
    }

    public void setAgentAccountName(String agentAccountName) {
        this.agentAccountName = agentAccountName;
    }

    public long getStatementMasterChangeSeqNum() {
        return statementMasterChangeSeqNum;
    }

    public void setStatementMasterChangeSeqNum(long statementMasterChangeSeqNum) {
        this.statementMasterChangeSeqNum = statementMasterChangeSeqNum;
    }

    public long getStatementLocalChangeSeqNum() {
        return statementLocalChangeSeqNum;
    }

    public void setStatementLocalChangeSeqNum(long statementLocalChangeSeqNum) {
        this.statementLocalChangeSeqNum = statementLocalChangeSeqNum;
    }

    public int getStatementLastChangedBy() {
        return statementLastChangedBy;
    }

    public void setStatementLastChangedBy(int statementLastChangedBy) {
        this.statementLastChangedBy = statementLastChangedBy;
    }
}
