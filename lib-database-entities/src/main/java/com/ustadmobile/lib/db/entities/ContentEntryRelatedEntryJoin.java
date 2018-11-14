package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a relationship between two ContentEntry items. This could be that one ContentEntry is
 * the translated version of another ContentEntry (relType = REL_TYPE_TRANSLATED_VERSION), or it
 * could be that the other entry is a see also link.
 */
//shortcode cerej
@UmEntity
public class ContentEntryRelatedEntryJoin {

    public static final int REL_TYPE_TRANSLATED_VERSION = 1;

    public static final int REL_TYPE_SEE_ALSO = 2;

    @UmPrimaryKey(autoIncrement = true)
    private long cerejUid;

    private long cerejContentEntryUid;

    private long cerejRelatedEntryUid;

    private int relType;

    private String cerejRelLanguage;

    private String comment;

    private long cerejRelLanguageUid;

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
}
