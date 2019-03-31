package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 61)
public class Sale {
    @UmPrimaryKey
    private long saleUid;

    private boolean saleActive;

    private boolean saleLocationUid;

    private long saleCreationDate;

    private long saleLastUpdateDate;

    private long salePersonUid;

    private String saleNotes;

    private boolean saleDone;

    private boolean saleCancelled;

    private boolean salePreOrder;

    @UmSyncMasterChangeSeqNum
    private long saleMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleLCSN;

    @UmSyncLastChangedBy
    private int saleLCB;

    public long getSaleUid() {
        return saleUid;
    }

    public void setSaleUid(long saleUid) {
        this.saleUid = saleUid;
    }

    public boolean isSaleActive() {
        return saleActive;
    }

    public void setSaleActive(boolean saleActive) {
        this.saleActive = saleActive;
    }

    public boolean isSaleLocationUid() {
        return saleLocationUid;
    }

    public void setSaleLocationUid(boolean saleLocationUid) {
        this.saleLocationUid = saleLocationUid;
    }

    public long getSaleCreationDate() {
        return saleCreationDate;
    }

    public void setSaleCreationDate(long saleCreationDate) {
        this.saleCreationDate = saleCreationDate;
    }

    public long getSaleLastUpdateDate() {
        return saleLastUpdateDate;
    }

    public void setSaleLastUpdateDate(long saleLastUpdateDate) {
        this.saleLastUpdateDate = saleLastUpdateDate;
    }

    public long getSalePersonUid() {
        return salePersonUid;
    }

    public void setSalePersonUid(long salePersonUid) {
        this.salePersonUid = salePersonUid;
    }

    public String getSaleNotes() {
        return saleNotes;
    }

    public void setSaleNotes(String saleNotes) {
        this.saleNotes = saleNotes;
    }

    public boolean isSaleDone() {
        return saleDone;
    }

    public void setSaleDone(boolean saleDone) {
        this.saleDone = saleDone;
    }

    public boolean isSaleCancelled() {
        return saleCancelled;
    }

    public void setSaleCancelled(boolean saleCancelled) {
        this.saleCancelled = saleCancelled;
    }

    public boolean isSalePreOrder() {
        return salePreOrder;
    }

    public void setSalePreOrder(boolean salePreOrder) {
        this.salePreOrder = salePreOrder;
    }

    public long getSaleMCSN() {
        return saleMCSN;
    }

    public void setSaleMCSN(long saleMCSN) {
        this.saleMCSN = saleMCSN;
    }

    public long getSaleLCSN() {
        return saleLCSN;
    }

    public void setSaleLCSN(long saleLCSN) {
        this.saleLCSN = saleLCSN;
    }

    public int getSaleLCB() {
        return saleLCB;
    }

    public void setSaleLCB(int saleLCB) {
        this.saleLCB = saleLCB;
    }
}
