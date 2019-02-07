package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents one run through of a question set for one particular student.
 */
@UmEntity(tableId = 27)
public class SelQuestionSetResponse {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionSetResposeUid;

    //-> SelQuestionSet - The Question Set
    private long selQuestionSetResponseSelQuestionSetUid;

    //clazz member doing this - The student (Class Member) doing this.
    private long selQuestionSetResponseClazzMemberUid;

    //start time
    private long selQuestionSetResponseStartTime;

    //finish time
    private long selQuestionSetResponseFinishTime;

    //total Response Recognition percentage. - to be calculated on device (not database).
    private float selQuestionSetResponseRecognitionPercentage;


    @UmSyncMasterChangeSeqNum
    private long selQuestionSetResponseMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionSetResponseLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionSetResponseLastChangedBy;

    public long getSelQuestionSetResponseClazzMemberUid() {
        return selQuestionSetResponseClazzMemberUid;
    }

    public void setSelQuestionSetResponseClazzMemberUid(long selQuestionSetResponseClazzMemberUid) {
        this.selQuestionSetResponseClazzMemberUid = selQuestionSetResponseClazzMemberUid;
    }

    public long getSelQuestionSetResponseMasterChangeSeqNum() {
        return selQuestionSetResponseMasterChangeSeqNum;
    }

    public void setSelQuestionSetResponseMasterChangeSeqNum(long selQuestionSetResponseMasterChangeSeqNum) {
        this.selQuestionSetResponseMasterChangeSeqNum = selQuestionSetResponseMasterChangeSeqNum;
    }

    public long getSelQuestionSetResponseLocalChangeSeqNum() {
        return selQuestionSetResponseLocalChangeSeqNum;
    }

    public void setSelQuestionSetResponseLocalChangeSeqNum(long selQuestionSetResponseLocalChangeSeqNum) {
        this.selQuestionSetResponseLocalChangeSeqNum = selQuestionSetResponseLocalChangeSeqNum;
    }

    public int getSelQuestionSetResponseLastChangedBy() {
        return selQuestionSetResponseLastChangedBy;
    }

    public void setSelQuestionSetResponseLastChangedBy(int selQuestionSetResponseLastChangedBy) {
        this.selQuestionSetResponseLastChangedBy = selQuestionSetResponseLastChangedBy;
    }

    public long getSelQuestionSetResponseStartTime() {
        return selQuestionSetResponseStartTime;
    }

    public void setSelQuestionSetResponseStartTime(long selQuestionSetResponseStartTime) {
        this.selQuestionSetResponseStartTime = selQuestionSetResponseStartTime;
    }

    public long getSelQuestionSetResponseFinishTime() {
        return selQuestionSetResponseFinishTime;
    }

    public void setSelQuestionSetResponseFinishTime(long selQuestionSetResponseFinishTime) {
        this.selQuestionSetResponseFinishTime = selQuestionSetResponseFinishTime;
    }

    public float getSelQuestionSetResponseRecognitionPercentage() {
        return selQuestionSetResponseRecognitionPercentage;
    }

    public void setSelQuestionSetResponseRecognitionPercentage(float percentage) {
        this.selQuestionSetResponseRecognitionPercentage = percentage;
    }

    public long getSelQuestionSetResposeUid() {
        return selQuestionSetResposeUid;
    }

    public void setSelQuestionSetResposeUid(long selQuestionSetResposeUid) {
        this.selQuestionSetResposeUid = selQuestionSetResposeUid;
    }

    public long getSelQuestionSetResponseSelQuestionSetUid() {
        return selQuestionSetResponseSelQuestionSetUid;
    }

    public void setSelQuestionSetResponseSelQuestionSetUid(long selQuestionSetResponseSelQuestionSetUid) {
        this.selQuestionSetResponseSelQuestionSetUid = selQuestionSetResponseSelQuestionSetUid;
    }
}
