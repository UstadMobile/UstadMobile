package com.ustadmobile.lib.db.entities;

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
public class SaleItemListDetail extends SaleItem{

    long saleItemPictureUid;
    String saleItemProductName;

    public String getSaleItemProductName() {
        return saleItemProductName;
    }

    public void setSaleItemProductName(String saleItemProductName) {
        this.saleItemProductName = saleItemProductName;
    }

    public long getSaleItemPictureUid() {
        return saleItemPictureUid;
    }

    public void setSaleItemPictureUid(long saleItemPictureUid) {
        this.saleItemPictureUid = saleItemPictureUid;
    }
}
