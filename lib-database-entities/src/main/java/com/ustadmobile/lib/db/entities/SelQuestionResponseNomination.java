package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the student who is nominated for a specific question in the question set
 */
@UmEntity(tableId = 24)
public class SelQuestionResponseNomination {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionResponseNominationUid;

    private long selQuestionResponseNominationClazzMemberUid;

    //<-> SelQuestionResponse entity.
    private long selQuestionResponseNominationSelQuestionResponseUId;

    //Added 31012019 sel TODO add to Migrations
    private boolean nominationActive;

    //Removed : TODO: add to Migrations
//    @UmSyncLastChangedBy
//    private int scheduleNominationQuestionResponseNominationLastChangedBy;

    //Renamed: TODO: add to Migrations
    @UmSyncMasterChangeSeqNum
    private long selQuestionResponseNominationMasterChangeSeqNum;
    //private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    //Renamed: TODO: add to Migrations
    @UmSyncLocalChangeSeqNum
    private long selQuestionResponseNominationLocalChangeSeqNum;
    //private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionResponseNominationLastChangedBy;


    public boolean isNominationActive() {
        return nominationActive;
    }

    public void setNominationActive(boolean nominationActive) {
        this.nominationActive = nominationActive;
    }

    public long getSelQuestionResponseNominationUid() {
        return selQuestionResponseNominationUid;
    }

    public void setSelQuestionResponseNominationUid(long selQuestionResponseNominationUid) {
        this.selQuestionResponseNominationUid = selQuestionResponseNominationUid;
    }

    public long getSelQuestionResponseNominationClazzMemberUid() {
        return selQuestionResponseNominationClazzMemberUid;
    }

    public void setSelQuestionResponseNominationClazzMemberUid(long selQuestionResponseNominationClazzMemberUid) {
        this.selQuestionResponseNominationClazzMemberUid = selQuestionResponseNominationClazzMemberUid;
    }

    public long getSelQuestionResponseNominationSelQuestionResponseUId() {
        return selQuestionResponseNominationSelQuestionResponseUId;
    }

    public void setSelQuestionResponseNominationSelQuestionResponseUId(long selQuestionResponseNominationSelQuestionResponseUId) {
        this.selQuestionResponseNominationSelQuestionResponseUId = selQuestionResponseNominationSelQuestionResponseUId;
    }

    public long getSelQuestionResponseNominationMasterChangeSeqNum() {
        return selQuestionResponseNominationMasterChangeSeqNum;
    }

    public void setSelQuestionResponseNominationMasterChangeSeqNum(long selQuestionResponseNominationMasterChangeSeqNum) {
        this.selQuestionResponseNominationMasterChangeSeqNum = selQuestionResponseNominationMasterChangeSeqNum;
    }

    public long getSelQuestionResponseNominationLocalChangeSeqNum() {
        return selQuestionResponseNominationLocalChangeSeqNum;
    }

    public void setSelQuestionResponseNominationLocalChangeSeqNum(long selQuestionResponseNominationLocalChangeSeqNum) {
        this.selQuestionResponseNominationLocalChangeSeqNum = selQuestionResponseNominationLocalChangeSeqNum;
    }

    public int getSelQuestionResponseNominationLastChangedBy() {
        return selQuestionResponseNominationLastChangedBy;
    }

    public void setSelQuestionResponseNominationLastChangedBy(int selQuestionResponseNominationLastChangedBy) {
        this.selQuestionResponseNominationLastChangedBy = selQuestionResponseNominationLastChangedBy;
    }
}
