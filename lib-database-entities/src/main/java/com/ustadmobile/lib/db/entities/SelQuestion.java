package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the question within a question set.
 * eg: "Select the students who sit alone"
 *
 */
@UmEntity(tableId = 22)
public class SelQuestion {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionUid;

    private String questionText;

    // -> SelQuestionSet - what set is this question a part of
    private long selQuestionSelQuestionSetUid;

    //The order.
    private int questionIndex;

    //If this question is to be assigned to all classes. (if not - not handled / implemented yet).
    boolean assignToAllClasses;

    //If this question allows for multiple nominations.
    boolean multiNominations;

    private int questionType;

    boolean questionActive;

    public boolean isQuestionActive() {
        return questionActive;
    }

    public void setQuestionActive(boolean questionActive) {
        this.questionActive = questionActive;
    }

    public int getQuestionType() {
        return questionType;
    }

    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    @UmSyncMasterChangeSeqNum
    private long selQuestionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionLastChangedBy;


    public boolean isAssignToAllClasses() {
        return assignToAllClasses;
    }

    public void setAssignToAllClasses(boolean assignToAllClasses) {
        this.assignToAllClasses = assignToAllClasses;
    }

    public boolean isMultiNominations() {
        return multiNominations;
    }

    public void setMultiNominations(boolean multiNominations) {
        this.multiNominations = multiNominations;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }


    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }


    public int getSelQuestionLastChangedBy() {
        return selQuestionLastChangedBy;
    }

    public void setSelQuestionLastChangedBy(int selQuestionLastChangedBy) {
        this.selQuestionLastChangedBy = selQuestionLastChangedBy;
    }

    public long getSelQuestionLocalChangeSeqNum() {
        return selQuestionLocalChangeSeqNum;
    }

    public void setSelQuestionLocalChangeSeqNum(long selQuestionLocalChangeSeqNum) {
        this.selQuestionLocalChangeSeqNum = selQuestionLocalChangeSeqNum;
    }

    public long getSelQuestionMasterChangeSeqNum() {
        return selQuestionMasterChangeSeqNum;
    }

    public void setSelQuestionMasterChangeSeqNum(long selQuestionMasterChangeSeqNum) {
        this.selQuestionMasterChangeSeqNum = selQuestionMasterChangeSeqNum;
    }

    public long getSelQuestionSelQuestionSetUid() {
        return selQuestionSelQuestionSetUid;
    }

    public void setSelQuestionSelQuestionSetUid(long selQuestionSelQuestionSetUid) {
        this.selQuestionSelQuestionSetUid = selQuestionSelQuestionSetUid;
    }

    public long getSelQuestionUid() {
        return selQuestionUid;
    }

    public void setSelQuestionUid(long selQuestionUid) {
        this.selQuestionUid = selQuestionUid;
    }
}
