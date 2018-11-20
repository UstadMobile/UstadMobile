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
    private int cerejUid;

    private int cerejContentEntryUid;

    private int cerejRelatedEntryUid;

    private int relType;

    private int cerejRelLanguage;

    private String comment;

    public int getCerejUid() {
        return cerejUid;
    }

    public void setCerejUid(int cerejUid) {
        this.cerejUid = cerejUid;
    }

    public int getCerejContentEntryUid() {
        return cerejContentEntryUid;
    }

    public void setCerejContentEntryUid(int cerejContentEntryUid) {
        this.cerejContentEntryUid = cerejContentEntryUid;
    }

    public int getCerejRelatedEntryUid() {
        return cerejRelatedEntryUid;
    }

    public void setCerejRelatedEntryUid(int cerejRelatedEntryUid) {
        this.cerejRelatedEntryUid = cerejRelatedEntryUid;
    }

    public int getRelType() {
        return relType;
    }

    public void setRelType(int relType) {
        this.relType = relType;
    }

    public int getCerejRelLanguage() {
        return cerejRelLanguage;
    }

    public void setCerejRelLanguage(int cerejRelLanguage) {
        this.cerejRelLanguage = cerejRelLanguage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
