package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * FOR RECOGNITION
 *
 * For each run through of a question set for one student, they must attempt to recognize their
 * classmates.
 * There is 1:many relationship between this entity and SelQuestionSetResponse.
 * There is one SelQuestionSetRecognition for each
 * SelQuestionSetResponse for each student in the class..
 *
 */
@UmEntity(tableId = 26)
public class SelQuestionSetRecognition {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionSetRecognitionUid;

    // -> SelQuestionSetResponse - The question set response (which has recognition percentages)
    private long selQuestionSetRecognitionSelQuestionSetResponseUid;

    // The Clazz Member - The Student To BE recognized.
    private long selQuestionSetRecognitionClazzMemberUid;

    // Boolean if recognized or not by the ClazzMember doing this QuestionSet in QuestionSetResponse.
    private boolean selQuestionSetRecognitionRecognized;

    @UmSyncMasterChangeSeqNum
    private long selQuestionSetRecognitionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionSetRecognitionLocalChangeSeqNum;


    @UmSyncLastChangedBy
    private int selQuestionSetRecognitionLastChangedBy;


    public long getSelQuestionSetRecognitionUid() {
        return selQuestionSetRecognitionUid;
    }

    public void setSelQuestionSetRecognitionUid(long selQuestionSetRecognitionUid) {
        this.selQuestionSetRecognitionUid = selQuestionSetRecognitionUid;
    }

    public long getSelQuestionSetRecognitionSelQuestionSetResponseUid() {
        return selQuestionSetRecognitionSelQuestionSetResponseUid;
    }

    public void setSelQuestionSetRecognitionSelQuestionSetResponseUid(long selQuestionSetRecognitionSelQuestionSetResponseUid) {
        this.selQuestionSetRecognitionSelQuestionSetResponseUid = selQuestionSetRecognitionSelQuestionSetResponseUid;
    }

    public long getSelQuestionSetRecognitionClazzMemberUid() {
        return selQuestionSetRecognitionClazzMemberUid;
    }

    public void setSelQuestionSetRecognitionClazzMemberUid(long selQuestionSetRecognitionClazzMemberUid) {
        this.selQuestionSetRecognitionClazzMemberUid = selQuestionSetRecognitionClazzMemberUid;
    }

    public boolean isSelQuestionSetRecognitionRecognized() {
        return selQuestionSetRecognitionRecognized;
    }

    public void setSelQuestionSetRecognitionRecognized(boolean selQuestionSetRecognitionRecognized) {
        this.selQuestionSetRecognitionRecognized = selQuestionSetRecognitionRecognized;
    }

    public long getSelQuestionSetRecognitionMasterChangeSeqNum() {
        return selQuestionSetRecognitionMasterChangeSeqNum;
    }

    public void setSelQuestionSetRecognitionMasterChangeSeqNum(long selQuestionSetRecognitionMasterChangeSeqNum) {
        this.selQuestionSetRecognitionMasterChangeSeqNum = selQuestionSetRecognitionMasterChangeSeqNum;
    }

    public long getSelQuestionSetRecognitionLocalChangeSeqNum() {
        return selQuestionSetRecognitionLocalChangeSeqNum;
    }

    public void setSelQuestionSetRecognitionLocalChangeSeqNum(long selQuestionSetRecognitionLocalChangeSeqNum) {
        this.selQuestionSetRecognitionLocalChangeSeqNum = selQuestionSetRecognitionLocalChangeSeqNum;
    }

    public int getSelQuestionSetRecognitionLastChangedBy() {
        return selQuestionSetRecognitionLastChangedBy;
    }

    public void setSelQuestionSetRecognitionLastChangedBy(int selQuestionSetRecognitionLastChangedBy) {
        this.selQuestionSetRecognitionLastChangedBy = selQuestionSetRecognitionLastChangedBy;
    }
}
