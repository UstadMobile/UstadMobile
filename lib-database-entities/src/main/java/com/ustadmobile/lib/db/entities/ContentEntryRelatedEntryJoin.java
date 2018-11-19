package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntry.TABLE_ID;

/**
 * Represents a relationship between two ContentEntry items. This could be that one ContentEntry is
 * the translated version of another ContentEntry (relType = REL_TYPE_TRANSLATED_VERSION), or it
 * could be that the other entry is a see also link.
 */
//shortcode cerej
@UmEntity(tableId = TABLE_ID)
public class ContentEntryRelatedEntryJoin {

    public static final int TABLE_ID = 8;

    public static final int REL_TYPE_TRANSLATED_VERSION = 1;

    public static final int REL_TYPE_SEE_ALSO = 2;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long cerejUid;

    private long cerejContentEntryUid;

    private long cerejRelatedEntryUid;

    private int relType;

    private String cerejRelLanguage;

    private String comment;

    private long cerejRelLanguageUid;

    @UmSyncLocalChangeSeqNum
    private long cerejLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long cerejMasterChangeSeqNum;

    public long getCerejUid() {
        return cerejUid;
    }

    public void setCerejUid(long cerejUid) {
        this.cerejUid = cerejUid;
    }

    public long getCerejContentEntryUid() {
        return cerejContentEntryUid;
    }

    public void setCerejContentEntryUid(long cerejContentEntryUid) {
        this.cerejContentEntryUid = cerejContentEntryUid;
    }

    public long getCerejRelatedEntryUid() {
        return cerejRelatedEntryUid;
    }

    public void setCerejRelatedEntryUid(long cerejRelatedEntryUid) {
        this.cerejRelatedEntryUid = cerejRelatedEntryUid;
    }

    public int getRelType() {
        return relType;
    }

    public void setRelType(int relType) {
        this.relType = relType;
    }

    public String getCerejRelLanguage() {
        return cerejRelLanguage;
    }

    public void setCerejRelLanguage(String cerejRelLanguage) {
        this.cerejRelLanguage = cerejRelLanguage;
    }

    public long getCerejRelLanguageUid() {
        return cerejRelLanguageUid;
    }

    public void setCerejRelLanguageUid(long cerejRelLanguageUid) {
        this.cerejRelLanguageUid = cerejRelLanguageUid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCerejLocalChangeSeqNum() {
        return cerejLocalChangeSeqNum;
    }

    public void setCerejLocalChangeSeqNum(long cerejLocalChangeSeqNum) {
        this.cerejLocalChangeSeqNum = cerejLocalChangeSeqNum;
    }

    public long getCerejMasterChangeSeqNum() {
        return cerejMasterChangeSeqNum;
    }

    public void setCerejMasterChangeSeqNum(long cerejMasterChangeSeqNum) {
        this.cerejMasterChangeSeqNum = cerejMasterChangeSeqNum;
    }
}
