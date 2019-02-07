package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the students response to a specific question in the question set
 */
@UmEntity(tableId = 23)
public class SelQuestionResponse {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionResponseUid;

    // -> SelQuestionSetResponse
    private long selQuestionResponseSelQuestionSetResponseUid;

    //Added the actual Question UID (28012019): TODO: Note for Migrations
    private long selQuestionResponseSelQuestionUid;

    @UmSyncMasterChangeSeqNum
    private long selQuestionResponseMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionResponseLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionResponseLastChangedBy;


    public long getSelQuestionResponseUid() {
        return selQuestionResponseUid;
    }

    public void setSelQuestionResponseUid(long selQuestionResponseUid) {
        this.selQuestionResponseUid = selQuestionResponseUid;
    }

    public long getSelQuestionResponseSelQuestionSetResponseUid() {
        return selQuestionResponseSelQuestionSetResponseUid;
    }

    public void setSelQuestionResponseSelQuestionSetResponseUid(long selQuestionResponseSelQuestionSetResponseUid) {
        this.selQuestionResponseSelQuestionSetResponseUid = selQuestionResponseSelQuestionSetResponseUid;
    }

    public long getSelQuestionResponseSelQuestionUid() {
        return selQuestionResponseSelQuestionUid;
    }

    public void setSelQuestionResponseSelQuestionUid(long selQuestionResponseSelQuestionUid) {
        this.selQuestionResponseSelQuestionUid = selQuestionResponseSelQuestionUid;
    }

    public long getSelQuestionResponseMasterChangeSeqNum() {
        return selQuestionResponseMasterChangeSeqNum;
    }

    public void setSelQuestionResponseMasterChangeSeqNum(long selQuestionResponseMasterChangeSeqNum) {
        this.selQuestionResponseMasterChangeSeqNum = selQuestionResponseMasterChangeSeqNum;
    }

    public long getSelQuestionResponseLocalChangeSeqNum() {
        return selQuestionResponseLocalChangeSeqNum;
    }

    public void setSelQuestionResponseLocalChangeSeqNum(long selQuestionResponseLocalChangeSeqNum) {
        this.selQuestionResponseLocalChangeSeqNum = selQuestionResponseLocalChangeSeqNum;
    }

    public int getSelQuestionResponseLastChangedBy() {
        return selQuestionResponseLastChangedBy;
    }

    public void setSelQuestionResponseLastChangedBy(int selQuestionResponseLastChangedBy) {
        this.selQuestionResponseLastChangedBy = selQuestionResponseLastChangedBy;
    }
}
