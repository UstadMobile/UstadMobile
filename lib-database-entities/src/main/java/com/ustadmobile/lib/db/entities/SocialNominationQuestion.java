package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the question within a question set.
 * eg: "Select the students who sit alone"
 *
 */
@UmEntity(tableId = 22)
public class SocialNominationQuestion {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionUid;

    private String questionText;

    // -> SocialNominationQuestionSet - what set is this question a part of
    private long socialNominationQuestionSocialNominationQuestionSetUid;

    //The order.
    private int questionIndex;

    //If this question is to be assigned to all classes. (if not - not handled / implemented yet).
    boolean assignToAllClasses;

    //If this question allows for multiple nominations.
    boolean multiNominations;


    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionLocalChangeSeqNum;

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

    public long getSocialNominationQuestionUid() {
        return socialNominationQuestionUid;
    }

    public void setSocialNominationQuestionUid(long socialNominationQuestionUid) {
        this.socialNominationQuestionUid = socialNominationQuestionUid;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public long getSocialNominationQuestionSocialNominationQuestionSetUid() {
        return socialNominationQuestionSocialNominationQuestionSetUid;
    }

    public void setSocialNominationQuestionSocialNominationQuestionSetUid(long socialNominationQuestionSocialNominationQuestionSetUid) {
        this.socialNominationQuestionSocialNominationQuestionSetUid = socialNominationQuestionSocialNominationQuestionSetUid;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public long getScheduleNominationQuestionMasterChangeSeqNum() {
        return scheduleNominationQuestionMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionMasterChangeSeqNum(long scheduleNominationQuestionMasterChangeSeqNum) {
        this.scheduleNominationQuestionMasterChangeSeqNum = scheduleNominationQuestionMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionLocalChangeSeqNum() {
        return scheduleNominationQuestionLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionLocalChangeSeqNum(long scheduleNominationQuestionLocalChangeSeqNum) {
        this.scheduleNominationQuestionLocalChangeSeqNum = scheduleNominationQuestionLocalChangeSeqNum;
    }
}
