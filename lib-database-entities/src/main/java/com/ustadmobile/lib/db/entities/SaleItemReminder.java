package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 79)
public class SaleItemReminder {


    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleItemReminderUid;

    private long saleItemReminderSaleItemUid;

    private int saleItemReminderDays;

    private boolean saleItemReminderActive;

    @UmSyncMasterChangeSeqNum
    private long saleItemReminderMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleItemReminderLCSN;

    @UmSyncLastChangedBy
    private int saleItemReminderLCB;

    public SaleItemReminder(){
        this.saleItemReminderActive = false;
    }

    public SaleItemReminder(boolean active){
        this.saleItemReminderActive = active;
    }

    public SaleItemReminder(int days, long saleItemUid, boolean active){
        this.saleItemReminderActive = active;
        this.saleItemReminderSaleItemUid = saleItemUid;
        this.saleItemReminderDays = days;
    }


    public long getSaleItemReminderUid() {
        return saleItemReminderUid;
    }

    public void setSaleItemReminderUid(long saleItemReminderUid) {
        this.saleItemReminderUid = saleItemReminderUid;
    }

    public long getSaleItemReminderSaleItemUid() {
        return saleItemReminderSaleItemUid;
    }

    public void setSaleItemReminderSaleItemUid(long saleItemReminderSaleItemUid) {
        this.saleItemReminderSaleItemUid = saleItemReminderSaleItemUid;
    }

    public int getSaleItemReminderDays() {
        return saleItemReminderDays;
    }

    public void setSaleItemReminderDays(int saleItemReminderDays) {
        this.saleItemReminderDays = saleItemReminderDays;
    }

    public long getSaleItemReminderMCSN() {
        return saleItemReminderMCSN;
    }

    public void setSaleItemReminderMCSN(long saleItemReminderMCSN) {
        this.saleItemReminderMCSN = saleItemReminderMCSN;
    }

    public long getSaleItemReminderLCSN() {
        return saleItemReminderLCSN;
    }

    public void setSaleItemReminderLCSN(long saleItemReminderLCSN) {
        this.saleItemReminderLCSN = saleItemReminderLCSN;
    }

    public int getSaleItemReminderLCB() {
        return saleItemReminderLCB;
    }

    public void setSaleItemReminderLCB(int saleItemReminderLCB) {
        this.saleItemReminderLCB = saleItemReminderLCB;
    }

    public boolean isSaleItemReminderActive() {
        return saleItemReminderActive;
    }

    public void setSaleItemReminderActive(boolean saleItemReminderActive) {
        this.saleItemReminderActive = saleItemReminderActive;
    }
}
