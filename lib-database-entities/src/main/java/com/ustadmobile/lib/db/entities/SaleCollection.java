package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 69)
public class SaleCollection {

    @UmPrimaryKey
    private long saleCollectionUid;

    //Name of collection. eg: Eid Collection
    private long saleCollectionName;

    //If collection is to be displayed from one start date
    private long saleCollectionStartDate;

    //If collection is to be displayed from one start date to one end date
    private long saleCollectionEndDate;

    @UmSyncMasterChangeSeqNum
    private long saleCollectionMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleCollectionLCSN;

    @UmSyncLastChangedBy
    private int saleCollectionLCB;


    public long getSaleCollectionUid() {
        return saleCollectionUid;
    }

    public void setSaleCollectionUid(long saleCollectionUid) {
        this.saleCollectionUid = saleCollectionUid;
    }

    public long getSaleCollectionName() {
        return saleCollectionName;
    }

    public void setSaleCollectionName(long saleCollectionName) {
        this.saleCollectionName = saleCollectionName;
    }

    public long getSaleCollectionStartDate() {
        return saleCollectionStartDate;
    }

    public void setSaleCollectionStartDate(long saleCollectionStartDate) {
        this.saleCollectionStartDate = saleCollectionStartDate;
    }

    public long getSaleCollectionEndDate() {
        return saleCollectionEndDate;
    }

    public void setSaleCollectionEndDate(long saleCollectionEndDate) {
        this.saleCollectionEndDate = saleCollectionEndDate;
    }

    public long getSaleCollectionMCSN() {
        return saleCollectionMCSN;
    }

    public void setSaleCollectionMCSN(long saleCollectionMCSN) {
        this.saleCollectionMCSN = saleCollectionMCSN;
    }

    public long getSaleCollectionLCSN() {
        return saleCollectionLCSN;
    }

    public void setSaleCollectionLCSN(long saleCollectionLCSN) {
        this.saleCollectionLCSN = saleCollectionLCSN;
    }

    public int getSaleCollectionLCB() {
        return saleCollectionLCB;
    }

    public void setSaleCollectionLCB(int saleCollectionLCB) {
        this.saleCollectionLCB = saleCollectionLCB;
    }
}
