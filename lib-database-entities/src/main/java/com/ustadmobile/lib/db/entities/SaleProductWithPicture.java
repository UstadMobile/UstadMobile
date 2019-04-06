package com.ustadmobile.lib.db.entities;

public class SaleProductWithPicture extends SaleProduct {
    private long saleProductPictureUid;

    @Override
    public long getSaleProductPictureUid() {
        return saleProductPictureUid;
    }

    @Override
    public void setSaleProductPictureUid(long saleProductPictureUid) {
        this.saleProductPictureUid = saleProductPictureUid;
    }
}
