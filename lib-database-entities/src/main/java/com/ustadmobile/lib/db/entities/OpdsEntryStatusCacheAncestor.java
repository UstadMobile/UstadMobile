package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Join entry used for the many-many relationship for OpdsEntryStatusCache, used to maintain a list of
 * all known ancestors for each entry. Join is in the form of descendent to ancestor using the
 * OpdsEntryStatusCache statusCacheUid field.
 */
@UmEntity
public class OpdsEntryStatusCacheAncestor {

    @UmPrimaryKey(autoIncrement = true)
    private int pkId;

    @UmIndexField
    private int opdsEntryStatusCacheId;

    @UmIndexField
    private int ancestorOpdsEntryStatusCacheId;

    public OpdsEntryStatusCacheAncestor() {

    }

    public OpdsEntryStatusCacheAncestor(int opdsEntryStatusCacheId, int ancestorOpdsEntryStatusCacheId) {
        this.opdsEntryStatusCacheId = opdsEntryStatusCacheId;
        this.ancestorOpdsEntryStatusCacheId = ancestorOpdsEntryStatusCacheId;
    }


    /**
     * The primary key id, an artificial auto-increment field.
     *
     * @return primary key id
     */
    public int getPkId() {
        return pkId;
    }

    public void setPkId(int pkId) {
        this.pkId = pkId;
    }

    /**
     * The statusCacheUid of the descendent.
     *
     * @return The statusCacheUid for of the descendent entry for this join.
     */
    public int getOpdsEntryStatusCacheId() {
        return opdsEntryStatusCacheId;
    }

    public void setOpdsEntryStatusCacheId(int opdsEntryStatusCacheId) {
        this.opdsEntryStatusCacheId = opdsEntryStatusCacheId;
    }

    /**
     * The statusCacheUid of the ancestor.
     *
     * @return The statusCacheUid of the ancestor for this join.
     */
    public int getAncestorOpdsEntryStatusCacheId() {
        return ancestorOpdsEntryStatusCacheId;
    }

    public void setAncestorOpdsEntryStatusCacheId(int ancestorOpdsEntryStatusCacheId) {
        this.ancestorOpdsEntryStatusCacheId = ancestorOpdsEntryStatusCacheId;
    }
}
