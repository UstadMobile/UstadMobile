package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 63)
public class SaleItem {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleItemUid;

    // The Sale uid
    private long saleItemSaleUid;

    //Producer person uid
    private long saleItemProducerUid;

    //Sale product uid
    private long saleItemProductUid;

    //Quantity of sale product
    private int saleItemQuantity;

    //Price per product
    private float saleItemPricePerPiece;

    //Currency (eg: Afs)
    private String saleItemCurrency;

    //If sold ticked.
    private boolean saleItemSold;

    //If pre order ticked.
    private boolean saleItemPreorder;

    //Any specific discount applied (not used at the moment)
    private float saleItemDiscount;

    //If active or not (false is effectively deleted
    // and is usually set for sale items not saved during creation)
    private boolean saleItemActive;

    //Date when the sale item was created (Usually current system time)
    private long saleItemCreationDate;

    //Due date of the sale item. Will only be used if saleItemPreorder is true.
    private long saleItemDueDate;

    //Stores the actual signature data
    private String saleItemSignature;

    @UmSyncMasterChangeSeqNum
    private long saleItemMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleItemLCSN;

    @UmSyncLastChangedBy
    private int saleItemLCB;

    public SaleItem(){
        this.setSaleItemCreationDate(System.currentTimeMillis());
        this.setSaleItemActive(false);
        this.setSaleItemSold(false);
        this.setSaleItemPreorder(false);
    }

    public SaleItem(long productUid){
        this.setSaleItemCreationDate(System.currentTimeMillis());
        this.setSaleItemActive(false);
        this.setSaleItemSold(true);
        this.setSaleItemPreorder(false);
        this.setSaleItemProductUid(productUid);
        this.setSaleItemPricePerPiece(0);
        this.setSaleItemQuantity(0);
    }

    public SaleItem(long productUid, int quantity, long ppp, long saleUid, long dueDate ){
        this.saleItemCurrency = "Afs";
        this.saleItemActive = true;
        this.saleItemCreationDate = System.currentTimeMillis();
        this.saleItemProductUid = productUid;
        this.saleItemQuantity = quantity;
        this.saleItemPricePerPiece = ppp;
        this.saleItemSaleUid = saleUid;
        this.saleItemDueDate = dueDate;
        this.setSaleItemSold(true);

    }

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

    public String getSaleItemSignature() {
        return saleItemSignature;
    }

    public void setSaleItemSignature(String saleItemSignature) {
        this.saleItemSignature = saleItemSignature;
    }
}
