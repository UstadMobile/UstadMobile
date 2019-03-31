package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 71)
public class SaleProductGroupJoin {

    @UmPrimaryKey
    private long saleProductGroupJoinUid;

    private long saleProductGroupJoinProductUid;

    private long saleProductGroupJoinCollectionUid;

    private boolean saleProductGroupJoinActive;


    @UmSyncMasterChangeSeqNum
    private long saleProductGroupJoinMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleProductGroupJoinLCSN;

    @UmSyncLastChangedBy
    private int saleProductGroupJoinLCB;


    public long getSaleProductGroupJoinUid() {
        return saleProductGroupJoinUid;
    }

    public void setSaleProductGroupJoinUid(long saleProductGroupJoinUid) {
        this.saleProductGroupJoinUid = saleProductGroupJoinUid;
    }

    public long getSaleProductGroupJoinProductUid() {
        return saleProductGroupJoinProductUid;
    }

    public void setSaleProductGroupJoinProductUid(long saleProductGroupJoinProductUid) {
        this.saleProductGroupJoinProductUid = saleProductGroupJoinProductUid;
    }

    public long getSaleProductGroupJoinCollectionUid() {
        return saleProductGroupJoinCollectionUid;
    }

    public void setSaleProductGroupJoinCollectionUid(long saleProductGroupJoinCollectionUid) {
        this.saleProductGroupJoinCollectionUid = saleProductGroupJoinCollectionUid;
    }

    public boolean isSaleProductGroupJoinActive() {
        return saleProductGroupJoinActive;
    }

    public void setSaleProductGroupJoinActive(boolean saleProductGroupJoinActive) {
        this.saleProductGroupJoinActive = saleProductGroupJoinActive;
    }

    public long getSaleProductGroupJoinMCSN() {
        return saleProductGroupJoinMCSN;
    }

    public void setSaleProductGroupJoinMCSN(long saleProductGroupJoinMCSN) {
        this.saleProductGroupJoinMCSN = saleProductGroupJoinMCSN;
    }

    public long getSaleProductGroupJoinLCSN() {
        return saleProductGroupJoinLCSN;
    }

    public void setSaleProductGroupJoinLCSN(long saleProductGroupJoinLCSN) {
        this.saleProductGroupJoinLCSN = saleProductGroupJoinLCSN;
    }

    public int getSaleProductGroupJoinLCB() {
        return saleProductGroupJoinLCB;
    }

    public void setSaleProductGroupJoinLCB(int saleProductGroupJoinLCB) {
        this.saleProductGroupJoinLCB = saleProductGroupJoinLCB;
    }
}
