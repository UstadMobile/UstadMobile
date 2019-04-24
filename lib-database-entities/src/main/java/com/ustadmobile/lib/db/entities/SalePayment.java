package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 65)
public class SalePayment {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long salePaymentUid;

    //The date the payment was made
    private long salePaymentPaidDate;

    //Paid amount
    private long salePaymentPaidAmount;

    //Currency of paid amount
    private String salePaymentCurrency;

    //Which sale is it attached to.
    private long salePaymentSaleUid;

    //Says the payment was done. If it is false, then the amount is outstanding.
    private boolean salePaymentDone;

    //If false, it wont show up on the app and reports - effectively deleted.
    private boolean salePaymentActive;

    @UmSyncMasterChangeSeqNum
    private long salePaymentMCSN;

    @UmSyncLocalChangeSeqNum
    private long salePaymentLCSN;

    @UmSyncLastChangedBy
    private int salePaymentLCB;

    public long getSalePaymentUid() {
        return salePaymentUid;
    }

    public void setSalePaymentUid(long salePaymentUid) {
        this.salePaymentUid = salePaymentUid;
    }

    public long getSalePaymentPaidDate() {
        return salePaymentPaidDate;
    }

    public void setSalePaymentPaidDate(long salePaymentPaidDate) {
        this.salePaymentPaidDate = salePaymentPaidDate;
    }

    public long getSalePaymentPaidAmount() {
        return salePaymentPaidAmount;
    }

    public void setSalePaymentPaidAmount(long salePaymentPaidAmount) {
        this.salePaymentPaidAmount = salePaymentPaidAmount;
    }

    public String getSalePaymentCurrency() {
        return salePaymentCurrency;
    }

    public void setSalePaymentCurrency(String salePaymentCurrency) {
        this.salePaymentCurrency = salePaymentCurrency;
    }

    public long getSalePaymentSaleUid() {
        return salePaymentSaleUid;
    }

    public void setSalePaymentSaleUid(long salePaymentSaleUid) {
        this.salePaymentSaleUid = salePaymentSaleUid;
    }

    public boolean isSalePaymentDone() {
        return salePaymentDone;
    }

    public void setSalePaymentDone(boolean salePaymentDone) {
        this.salePaymentDone = salePaymentDone;
    }

    public boolean isSalePaymentActive() {
        return salePaymentActive;
    }

    public void setSalePaymentActive(boolean salePaymentActive) {
        this.salePaymentActive = salePaymentActive;
    }

    public long getSalePaymentMCSN() {
        return salePaymentMCSN;
    }

    public void setSalePaymentMCSN(long salePaymentMCSN) {
        this.salePaymentMCSN = salePaymentMCSN;
    }

    public long getSalePaymentLCSN() {
        return salePaymentLCSN;
    }

    public void setSalePaymentLCSN(long salePaymentLCSN) {
        this.salePaymentLCSN = salePaymentLCSN;
    }

    public int getSalePaymentLCB() {
        return salePaymentLCB;
    }

    public void setSalePaymentLCB(int salePaymentLCB) {
        this.salePaymentLCB = salePaymentLCB;
    }
}
