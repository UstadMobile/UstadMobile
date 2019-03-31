package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 63)
public class SaleItem {

    @UmPrimaryKey
    private long saleItemUid;

    // The Sale uid
    private long saleItemSaleUid;

    //Producer person uid
    private long saleItemProducerUid;

    //Product id
    private long saleItemProductUid;

    private int saleItemQuantity;

    private float saleItemPricePerPiece;

    private String saleItemCurrency;

    private boolean saleItemSold;

    private boolean saleItemPreorder;

    private float saleItemDiscount;

    private boolean saleItemActive;

    private long saleItemCreationDate;


    private long saleItemDueDate;

    @UmSyncMasterChangeSeqNum
    private long saleItemMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleItemLCSN;

    @UmSyncLastChangedBy
    private int saleItemLCB;

    public long getSaleItemUid() {
        return saleItemUid;
    }

    public void setSaleItemUid(long saleItemUid) {
        this.saleItemUid = saleItemUid;
    }

    public long getSaleItemSaleUid() {
        return saleItemSaleUid;
    }

    public void setSaleItemSaleUid(long saleItemSaleUid) {
        this.saleItemSaleUid = saleItemSaleUid;
    }

    public long getSaleItemProducerUid() {
        return saleItemProducerUid;
    }

    public void setSaleItemProducerUid(long saleItemProducerUid) {
        this.saleItemProducerUid = saleItemProducerUid;
    }

    public long getSaleItemProductUid() {
        return saleItemProductUid;
    }

    public void setSaleItemProductUid(long saleItemProductUid) {
        this.saleItemProductUid = saleItemProductUid;
    }

    public int getSaleItemQuantity() {
        return saleItemQuantity;
    }

    public void setSaleItemQuantity(int saleItemQuantity) {
        this.saleItemQuantity = saleItemQuantity;
    }

    public float getSaleItemPricePerPiece() {
        return saleItemPricePerPiece;
    }

    public void setSaleItemPricePerPiece(float saleItemPricePerPiece) {
        this.saleItemPricePerPiece = saleItemPricePerPiece;
    }

    public String getSaleItemCurrency() {
        return saleItemCurrency;
    }

    public void setSaleItemCurrency(String saleItemCurrency) {
        this.saleItemCurrency = saleItemCurrency;
    }

    public boolean isSaleItemSold() {
        return saleItemSold;
    }

    public void setSaleItemSold(boolean saleItemSold) {
        this.saleItemSold = saleItemSold;
    }

    public boolean isSaleItemPreorder() {
        return saleItemPreorder;
    }

    public void setSaleItemPreorder(boolean saleItemPreorder) {
        this.saleItemPreorder = saleItemPreorder;
    }

    public float getSaleItemDiscount() {
        return saleItemDiscount;
    }

    public void setSaleItemDiscount(float saleItemDiscount) {
        this.saleItemDiscount = saleItemDiscount;
    }

    public boolean isSaleItemActive() {
        return saleItemActive;
    }

    public void setSaleItemActive(boolean saleItemActive) {
        this.saleItemActive = saleItemActive;
    }

    public long getSaleItemDueDate() {
        return saleItemDueDate;
    }

    public void setSaleItemDueDate(long saleItemDueDate) {
        this.saleItemDueDate = saleItemDueDate;
    }

    public long getSaleItemCreationDate() {
        return saleItemCreationDate;
    }

    public void setSaleItemCreationDate(long saleItemCreationDate) {
        this.saleItemCreationDate = saleItemCreationDate;
    }

    public long getSaleItemMCSN() {
        return saleItemMCSN;
    }

    public void setSaleItemMCSN(long saleItemMCSN) {
        this.saleItemMCSN = saleItemMCSN;
    }

    public long getSaleItemLCSN() {
        return saleItemLCSN;
    }

    public void setSaleItemLCSN(long saleItemLCSN) {
        this.saleItemLCSN = saleItemLCSN;
    }

    public int getSaleItemLCB() {
        return saleItemLCB;
    }

    public void setSaleItemLCB(int saleItemLCB) {
        this.saleItemLCB = saleItemLCB;
    }
}
