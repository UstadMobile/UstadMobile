package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@UmEntity(tableId = 25)
public class SelQuestionSet {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionSetUid;

    // The set title.
    private String title;

    @UmSyncMasterChangeSeqNum
    private long selQuestionSetMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionSetLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionSetLastChangedBy;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSelQuestionSetMasterChangeSeqNum() {
        return selQuestionSetMasterChangeSeqNum;
    }

    public void setSelQuestionSetMasterChangeSeqNum(long selQuestionSetMasterChangeSeqNum) {
        this.selQuestionSetMasterChangeSeqNum = selQuestionSetMasterChangeSeqNum;
    }

    public long getSelQuestionSetLocalChangeSeqNum() {
        return selQuestionSetLocalChangeSeqNum;
    }

    public void setSelQuestionSetLocalChangeSeqNum(long selQuestionSetLocalChangeSeqNum) {
        this.selQuestionSetLocalChangeSeqNum = selQuestionSetLocalChangeSeqNum;
    }

    public int getSelQuestionSetLastChangedBy() {
        return selQuestionSetLastChangedBy;
    }

    public void setSelQuestionSetLastChangedBy(int selQuestionSetLastChangedBy) {
        this.selQuestionSetLastChangedBy = selQuestionSetLastChangedBy;
    }

    public long getSelQuestionSetUid() {
        return selQuestionSetUid;
    }

    public void setSelQuestionSetUid(long selQuestionSetUid) {
        this.selQuestionSetUid = selQuestionSetUid;
    }
}
