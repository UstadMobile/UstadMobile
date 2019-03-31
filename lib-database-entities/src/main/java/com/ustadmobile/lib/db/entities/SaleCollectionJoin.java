package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 71)
public class SaleCollectionJoin {

    @UmPrimaryKey
    private long saleCollectionJoinUid;

    private long saleCollectionJoinProductUid;

    private long saleCollectionJoinCollectionUid;


    @UmSyncMasterChangeSeqNum
    private long saleCollectionJoinMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleCollectionJoinLCSN;

    @UmSyncLastChangedBy
    private int saleCollectionJoinLCB;


    public long getSaleCollectionJoinUid() {
        return saleCollectionJoinUid;
    }

    public void setSaleCollectionJoinUid(long saleCollectionJoinUid) {
        this.saleCollectionJoinUid = saleCollectionJoinUid;
    }

    public long getSaleCollectionJoinProductUid() {
        return saleCollectionJoinProductUid;
    }

    public void setSaleCollectionJoinProductUid(long saleCollectionJoinProductUid) {
        this.saleCollectionJoinProductUid = saleCollectionJoinProductUid;
    }

    public long getSaleCollectionJoinCollectionUid() {
        return saleCollectionJoinCollectionUid;
    }

    public void setSaleCollectionJoinCollectionUid(long saleCollectionJoinCollectionUid) {
        this.saleCollectionJoinCollectionUid = saleCollectionJoinCollectionUid;
    }

    public long getSaleCollectionJoinMCSN() {
        return saleCollectionJoinMCSN;
    }

    public void setSaleCollectionJoinMCSN(long saleCollectionJoinMCSN) {
        this.saleCollectionJoinMCSN = saleCollectionJoinMCSN;
    }

    public long getSaleCollectionJoinLCSN() {
        return saleCollectionJoinLCSN;
    }

    public void setSaleCollectionJoinLCSN(long saleCollectionJoinLCSN) {
        this.saleCollectionJoinLCSN = saleCollectionJoinLCSN;
    }

    public int getSaleCollectionJoinLCB() {
        return saleCollectionJoinLCB;
    }

    public void setSaleCollectionJoinLCB(int saleCollectionJoinLCB) {
        this.saleCollectionJoinLCB = saleCollectionJoinLCB;
    }
}
