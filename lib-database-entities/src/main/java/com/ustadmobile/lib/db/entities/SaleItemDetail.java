package com.ustadmobile.lib.db.entities;

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
public class SaleItemDetail {

    long saleItemPictureUid;
    String saleItemProductName;
    int saleItemQuantityCount;
    float saleItemPrice;
    float saleItemDiscountPerItem;
    boolean saleItemDelivered;

    public long getSaleItemPictureUid() {
        return saleItemPictureUid;
    }

    public void setSaleItemPictureUid(long saleItemPictureUid) {
        this.saleItemPictureUid = saleItemPictureUid;
    }

    public String getSaleItemProductName() {
        return saleItemProductName;
    }

    public void setSaleItemProductName(String saleItemProductName) {
        this.saleItemProductName = saleItemProductName;
    }

    public int getSaleItemQuantityCount() {
        return saleItemQuantityCount;
    }

    public void setSaleItemQuantityCount(int saleItemQuantityCount) {
        this.saleItemQuantityCount = saleItemQuantityCount;
    }

    public float getSaleItemPrice() {
        return saleItemPrice;
    }

    public void setSaleItemPrice(float saleItemPrice) {
        this.saleItemPrice = saleItemPrice;
    }

    public float getSaleItemDiscountPerItem() {
        return saleItemDiscountPerItem;
    }

    public void setSaleItemDiscountPerItem(float saleItemDiscountPerItem) {
        this.saleItemDiscountPerItem = saleItemDiscountPerItem;
    }

    public boolean isSaleItemDelivered() {
        return saleItemDelivered;
    }

    public void setSaleItemDelivered(boolean saleItemDelivered) {
        this.saleItemDelivered = saleItemDelivered;
    }
}
