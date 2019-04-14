package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 73)
public class SaleProductPicture {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleProductPictureUid;

    //The product uid
    private long saleProductPictureSaleProductUid;

    //file size of product picture
    private int saleProductPictureFileSize;

    //picture file's timestamp
    private long saleProductPictureTimestamp;

    //picture file's mime type
    private String saleProductPictureMime;

    @UmSyncMasterChangeSeqNum
    private long saleProductPictureMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleProductPictureLCSN;

    @UmSyncLastChangedBy
    private int saleProductPictureLCB;


    public long getSaleProductPictureUid() {
        return saleProductPictureUid;
    }

    public void setSaleProductPictureUid(long saleProductPictureUid) {
        this.saleProductPictureUid = saleProductPictureUid;
    }

    public long getSaleProductPictureSaleProductUid() {
        return saleProductPictureSaleProductUid;
    }

    public void setSaleProductPictureSaleProductUid(long saleProductPictureSaleProductUid) {
        this.saleProductPictureSaleProductUid = saleProductPictureSaleProductUid;
    }

    public int getSaleProductPictureFileSize() {
        return saleProductPictureFileSize;
    }

    public void setSaleProductPictureFileSize(int saleProductPictureFileSize) {
        this.saleProductPictureFileSize = saleProductPictureFileSize;
    }

    public long getSaleProductPictureTimestamp() {
        return saleProductPictureTimestamp;
    }

    public void setSaleProductPictureTimestamp(long saleProductPictureTimestamp) {
        this.saleProductPictureTimestamp = saleProductPictureTimestamp;
    }

    public String getSaleProductPictureMime() {
        return saleProductPictureMime;
    }

    public void setSaleProductPictureMime(String saleProductPictureMime) {
        this.saleProductPictureMime = saleProductPictureMime;
    }

    public long getSaleProductPictureMCSN() {
        return saleProductPictureMCSN;
    }

    public void setSaleProductPictureMCSN(long saleProductPictureMCSN) {
        this.saleProductPictureMCSN = saleProductPictureMCSN;
    }

    public long getSaleProductPictureLCSN() {
        return saleProductPictureLCSN;
    }

    public void setSaleProductPictureLCSN(long saleProductPictureLCSN) {
        this.saleProductPictureLCSN = saleProductPictureLCSN;
    }

    public int getSaleProductPictureLCB() {
        return saleProductPictureLCB;
    }

    public void setSaleProductPictureLCB(int saleProductPictureLCB) {
        this.saleProductPictureLCB = saleProductPictureLCB;
    }
}
