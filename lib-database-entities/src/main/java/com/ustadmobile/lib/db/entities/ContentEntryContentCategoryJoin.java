package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@UmEntity
public class ContentEntryContentCategoryJoin {

    @UmPrimaryKey(autoIncrement = true)
    private long ceccjUid;

    private long ceccjContentEntryUid;

    private long ceccjContentCategoryUid;

    public long getCeccjUid() {
        return ceccjUid;
    }

    public void setCeccjUid(long ceccjUid) {
        this.ceccjUid = ceccjUid;
    }

    public long getCeccjContentEntryUid() {
        return ceccjContentEntryUid;
    }

    public void setCeccjContentEntryUid(long ceccjContentEntryUid) {
        this.ceccjContentEntryUid = ceccjContentEntryUid;
    }

    public long getCeccjContentCategoryUid() {
        return ceccjContentCategoryUid;
    }

    public void setCeccjContentCategoryUid(long ceccjContentCategoryUid) {
        this.ceccjContentCategoryUid = ceccjContentCategoryUid;
    }
}
