package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.StatementEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class StatementEntity {

    public static final int TABLE_ID = 60;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long statementUid;

    private String statementId;

    private long personUid;

    private long verbUid;

    private long XObjectUid;

    private long subStatementActorUid;

    private long substatementVerbUid;

    private long subStatementObjectUid;

    private long agentUid;

    private long instructorUid;

    private long authorityUid;

    private long teamUid;

    private boolean resultCompletion;

    private boolean resultSuccess;

    private long resultScoreScaled;

    private long resultScoreRaw;

    private long resultScoreMin;

    private long resultScoreMax;

    private long resultDuration;

    private String resultResponse;

    private long timestamp;

    private long stored;

    private String contextRegistration;

    private String contextPlatform;

    private String contextStatementId;

    private String fullStatement;

    @UmSyncMasterChangeSeqNum
    private long statementMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long statementLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int statementLastChangedBy;


    public long getStatementUid() {
        return statementUid;
    }

    public void setStatementUid(long statementUid) {
        this.statementUid = statementUid;
    }

    public String getStatementId() {
        return statementId;
    }

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }

    public long getVerbUid() {
        return verbUid;
    }

    public void setVerbUid(long verbUid) {
        this.verbUid = verbUid;
    }

    public long getXObjectUid() {
        return XObjectUid;
    }

    public void setXObjectUid(long XObjectUid) {
        this.XObjectUid = XObjectUid;
    }

    public boolean isResultCompletion() {
        return resultCompletion;
    }

    public void setResultCompletion(boolean resultCompletion) {
        this.resultCompletion = resultCompletion;
    }

    public boolean isResultSuccess() {
        return resultSuccess;
    }

    public void setResultSuccess(boolean resultSuccess) {
        this.resultSuccess = resultSuccess;
    }

    public long getResultScoreScaled() {
        return resultScoreScaled;
    }

    public void setResultScoreScaled(long resultScoreScaled) {
        this.resultScoreScaled = resultScoreScaled;
    }

    public long getResultScoreRaw() {
        return resultScoreRaw;
    }

    public void setResultScoreRaw(long resultScoreRaw) {
        this.resultScoreRaw = resultScoreRaw;
    }

    public long getResultScoreMin() {
        return resultScoreMin;
    }

    public void setResultScoreMin(long resultScoreMin) {
        this.resultScoreMin = resultScoreMin;
    }

    public long getResultScoreMax() {
        return resultScoreMax;
    }

    public void setResultScoreMax(long resultScoreMax) {
        this.resultScoreMax = resultScoreMax;
    }

    public long getResultDuration() {
        return resultDuration;
    }

    public void setResultDuration(long resultDuration) {
        this.resultDuration = resultDuration;
    }

    public String getResultResponse() {
        return resultResponse;
    }

    public void setResultResponse(String resultResponse) {
        this.resultResponse = resultResponse;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getStored() {
        return stored;
    }

    public void setStored(long stored) {
        this.stored = stored;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public void setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
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

    public String getContextRegistration() {
        return contextRegistration;
    }

    public void setContextRegistration(String contextRegistration) {
        this.contextRegistration = contextRegistration;
    }

    public String getContextPlatform() {
        return contextPlatform;
    }

    public void setContextPlatform(String contextPlatform) {
        this.contextPlatform = contextPlatform;
    }

    public String getContextStatementId() {
        return contextStatementId;
    }

    public void setContextStatementId(String contextStatementUid) {
        this.contextStatementId = contextStatementUid;
    }

    public long getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(long agentUid) {
        this.agentUid = agentUid;
    }

    public long getInstructorUid() {
        return instructorUid;
    }

    public void setInstructorUid(long instructorUid) {
        this.instructorUid = instructorUid;
    }

    public long getAuthorityUid() {
        return authorityUid;
    }

    public void setAuthorityUid(long authorityUid) {
        this.authorityUid = authorityUid;
    }

    public long getTeamUid() {
        return teamUid;
    }

    public void setTeamUid(long teamUid) {
        this.teamUid = teamUid;
    }

    public long getSubStatementActorUid() {
        return subStatementActorUid;
    }

    public void setSubStatementActorUid(long subStatementActorUid) {
        this.subStatementActorUid = subStatementActorUid;
    }

    public long getSubstatementVerbUid() {
        return substatementVerbUid;
    }

    public void setSubstatementVerbUid(long substatementVerbUid) {
        this.substatementVerbUid = substatementVerbUid;
    }

    public long getSubStatementObjectUid() {
        return subStatementObjectUid;
    }

    public void setSubStatementObjectUid(long subStatementObjectUid) {
        this.subStatementObjectUid = subStatementObjectUid;
    }
}
