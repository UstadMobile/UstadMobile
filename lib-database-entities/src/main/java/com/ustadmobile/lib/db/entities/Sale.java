package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 61)
public class Sale {
    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleUid;

    private String saleTitle;

    private boolean saleActive;

    private long saleLocationUid;

    private long saleCreationDate;

    private long saleDueDate;

    private long saleLastUpdateDate;

    //Person who created this sale (the salesman usually)
    private long salePersonUid;

    //any notes
    private String saleNotes;

    //If created successfully - does NOT indicate payed / completed/ etc
    //If false- effectively deleted and will not show up in reports/sales list
    private boolean saleDone;

    //Deliberate cancelled option will mark this flag as true.
    private boolean saleCancelled;

    private boolean salePreOrder;

    private boolean salePaymentDone;

    private long saleDiscount;

    private String saleSignature;

    @UmSyncMasterChangeSeqNum
    private long saleMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleLCSN;

    @UmSyncLastChangedBy
    private int saleLCB;

    public Sale(){
        this.saleCancelled = false;
        this.saleActive = true; // false is essentially deleted.
        this.saleCreationDate = System.currentTimeMillis();
        this.saleLastUpdateDate = this.saleCreationDate;
        this.saleDone = false; // It gets done only when Sale is delivered.
        this.salePreOrder = true; //default to true
        this.salePaymentDone = true; //Defaulting to true. Unless marked as done via payment addition.
        this.saleTitle = "";
        //Ideally salePaymentDone should be triggered from SaleItem and SalePayment
    }

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

    public long getSaleLocationUid() {
        return saleLocationUid;
    }

    public void setSaleLocationUid(long saleLocationUid) {
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

    public boolean isSalePaymentDone() {
        return salePaymentDone;
    }

    public void setSalePaymentDone(boolean salePaymentDone) {
        this.salePaymentDone = salePaymentDone;
    }

    public String getSaleTitle() {
        return saleTitle;
    }

    public void setSaleTitle(String saleTitle) {
        this.saleTitle = saleTitle;
    }

    public long getSaleDueDate() {
        return saleDueDate;
    }

    public void setSaleDueDate(long saleDueDate) {
        this.saleDueDate = saleDueDate;
    }

    public long getSaleDiscount() {
        return saleDiscount;
    }

    public void setSaleDiscount(long saleDiscount) {
        this.saleDiscount = saleDiscount;
    }

    public String getSaleSignature() {
        return saleSignature;
    }

    public void setSaleSignature(String saleSignature) {
        this.saleSignature = saleSignature;
    }
}
