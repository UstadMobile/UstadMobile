package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity
public class SaleProductParentJoin {

    @UmPrimaryKey(autoIncrement = true)
    private long saleProductParentJoinUid;

    //Parent product or category eg: Science Books
    private long saleProductParentJoinParentUid;

    //Child product eg: A brief history of time
    private long saleProductParentJoinChildUid;

    private boolean saleProductParentJoinActive;

    private long saleProductParentJoinDateCreated;

    @UmSyncMasterChangeSeqNum
    private long saleProductParentJoinMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleProductParentJoinLCSN;

    @UmSyncLastChangedBy
    private int saleProductParentJoinLCB;

    public SaleProductParentJoin(){}

    public SaleProductParentJoin(long childUid, long parentUid){
        this.saleProductParentJoinParentUid = parentUid;
        this.saleProductParentJoinChildUid = childUid;
        this.saleProductParentJoinActive = false;
        this.saleProductParentJoinDateCreated = System.currentTimeMillis();
    }

    public SaleProductParentJoin(long childUid, long parentUid, boolean activate){
        this.saleProductParentJoinParentUid = parentUid;
        this.saleProductParentJoinChildUid = childUid;
        this.saleProductParentJoinDateCreated = System.currentTimeMillis();
        this.setSaleProductParentJoinActive(activate);
    }

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

    public boolean isSaleProductParentJoinActive() {
        return saleProductParentJoinActive;
    }

    public void setSaleProductParentJoinActive(boolean saleProductParentJoinActive) {
        this.saleProductParentJoinActive = saleProductParentJoinActive;
    }

    public long getSaleProductParentJoinDateCreated() {
        return saleProductParentJoinDateCreated;
    }

    public void setSaleProductParentJoinDateCreated(long saleProductParentJoinDateCreated) {
        this.saleProductParentJoinDateCreated = saleProductParentJoinDateCreated;
    }

    public long getSaleProductParentJoinMCSN() {
        return saleProductParentJoinMCSN;
    }

    public void setSaleProductParentJoinMCSN(long saleProductParentJoinMCSN) {
        this.saleProductParentJoinMCSN = saleProductParentJoinMCSN;
    }

    public long getSaleProductParentJoinLCSN() {
        return saleProductParentJoinLCSN;
    }

    public void setSaleProductParentJoinLCSN(long saleProductParentJoinLCSN) {
        this.saleProductParentJoinLCSN = saleProductParentJoinLCSN;
    }

    public int getSaleProductParentJoinLCB() {
        return saleProductParentJoinLCB;
    }

    public void setSaleProductParentJoinLCB(int saleProductParentJoinLCB) {
        this.saleProductParentJoinLCB = saleProductParentJoinLCB;
    }
}
