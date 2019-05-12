package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class SaleProductParentJoin {

    @UmPrimaryKey(autoIncrement = true)
    private long saleProductParentJoinUid;

    //Parent product or category eg: Science Books
    private long saleProductParentJoinParentUid;

    //Child product eg: A brief history of time
    private long saleProductParentJoinChildUid;

    SaleProductParentJoin(){}

    /* GETTERS AND SETTER */

    public long getSaleProductParentJoinUid() {
        return saleProductParentJoinUid;
    }

    public void setSaleProductParentJoinUid(long saleProductParentJoinUid) {
        this.saleProductParentJoinUid = saleProductParentJoinUid;
    }

    public long getSaleProductParentJoinParentUid() {
        return saleProductParentJoinParentUid;
    }

    public void setSaleProductParentJoinParentUid(long saleProductParentJoinParentUid) {
        this.saleProductParentJoinParentUid = saleProductParentJoinParentUid;
    }

    public long getSaleProductParentJoinChildUid() {
        return saleProductParentJoinChildUid;
    }

    public void setSaleProductParentJoinChildUid(long saleProductParentJoinChildUid) {
        this.saleProductParentJoinChildUid = saleProductParentJoinChildUid;
    }
}
