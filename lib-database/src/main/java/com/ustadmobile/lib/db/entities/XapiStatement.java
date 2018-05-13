package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

public class XapiStatement {

    @UmPrimaryKey
    private String uuid;

    private long xapiAgentUid;

    private long xapiActivityUid;

    private long xapiActorUid;

    private long verbUid;

    private long authorityUid;

    private String statementRef;

    private boolean resultSuccess;

    private boolean resultComplete;

    private String resultResponse;

    private long resultDuration;

    private float resultScoreScaled;

    private float resultScaleRaw;

    private float resultScoreMin;

    private float resultScoreMax;

    private String resultExtensions;

    private int resultProgress;

    private long stored;

    private long timestamp;

    private String contextRegistration;

    private String version;

    private String fullStatement;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getXapiAgentUid() {
        return xapiAgentUid;
    }

    public void setXapiAgentUid(long xapiAgentUid) {
        this.xapiAgentUid = xapiAgentUid;
    }

    public long getXapiActivityUid() {
        return xapiActivityUid;
    }

    public void setXapiActivityUid(long xapiActivityUid) {
        this.xapiActivityUid = xapiActivityUid;
    }

    public long getXapiActorUid() {
        return xapiActorUid;
    }

    public void setXapiActorUid(long xapiActorUid) {
        this.xapiActorUid = xapiActorUid;
    }

    public long getVerbUid() {
        return verbUid;
    }

    public void setVerbUid(long verbUid) {
        this.verbUid = verbUid;
    }

    public long getAuthorityUid() {
        return authorityUid;
    }

    public void setAuthorityUid(long authorityUid) {
        this.authorityUid = authorityUid;
    }

    public String getStatementRef() {
        return statementRef;
    }

    public void setStatementRef(String statementRef) {
        this.statementRef = statementRef;
    }

    public boolean isResultSuccess() {
        return resultSuccess;
    }

    public void setResultSuccess(boolean resultSuccess) {
        this.resultSuccess = resultSuccess;
    }

    public boolean isResultComplete() {
        return resultComplete;
    }

    public void setResultComplete(boolean resultComplete) {
        this.resultComplete = resultComplete;
    }

    public String getResultResponse() {
        return resultResponse;
    }

    public void setResultResponse(String resultResponse) {
        this.resultResponse = resultResponse;
    }

    public long getResultDuration() {
        return resultDuration;
    }

    public void setResultDuration(long resultDuration) {
        this.resultDuration = resultDuration;
    }

    public float getResultScoreScaled() {
        return resultScoreScaled;
    }

    public void setResultScoreScaled(float resultScoreScaled) {
        this.resultScoreScaled = resultScoreScaled;
    }

    public float getResultScaleRaw() {
        return resultScaleRaw;
    }

    public void setResultScaleRaw(float resultScaleRaw) {
        this.resultScaleRaw = resultScaleRaw;
    }

    public float getResultScoreMin() {
        return resultScoreMin;
    }

    public void setResultScoreMin(float resultScoreMin) {
        this.resultScoreMin = resultScoreMin;
    }

    public float getResultScoreMax() {
        return resultScoreMax;
    }

    public void setResultScoreMax(float resultScoreMax) {
        this.resultScoreMax = resultScoreMax;
    }

    public String getResultExtensions() {
        return resultExtensions;
    }

    public void setResultExtensions(String resultExtensions) {
        this.resultExtensions = resultExtensions;
    }

    public int getResultProgress() {
        return resultProgress;
    }

    public void setResultProgress(int resultProgress) {
        this.resultProgress = resultProgress;
    }

    public long getStored() {
        return stored;
    }

    public void setStored(long stored) {
        this.stored = stored;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContextRegistration() {
        return contextRegistration;
    }

    public void setContextRegistration(String contextRegistration) {
        this.contextRegistration = contextRegistration;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public void setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
    }
}
