package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@UmEntity
public class ContentEntryParentChildJoin {

    @UmPrimaryKey(autoIncrement = true)
    private long cepcjUid;

    private long cepcjChildContentEntryUid;

    private long cepcjParentContentEntryUid;

    private int childIndex;

    public long getCepcjUid() {
        return cepcjUid;
    }

    public void setCepcjUid(long cepcjUid) {
        this.cepcjUid = cepcjUid;
    }

    public long getCepcjChildContentEntryUid() {
        return cepcjChildContentEntryUid;
    }

    public void setCepcjChildContentEntryUid(long cepcjChildContentEntryUid) {
        this.cepcjChildContentEntryUid = cepcjChildContentEntryUid;
    }

    public long getCepcjParentContentEntryUid() {
        return cepcjParentContentEntryUid;
    }

    public void setCepcjParentContentEntryUid(long cepcjParentContentEntryUid) {
        this.cepcjParentContentEntryUid = cepcjParentContentEntryUid;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }
}
