package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 67)
public class SaleProduct {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleProductUid;

    //Name eg: Blue ribbon
    private String saleProductName;

    private String saleProductNameDari;

    private String saleProductDescDari;

    private String saleProductNamePashto;

    private String saleProductDescPashto;

    //Description eg: A blue ribbon used for gift wrapping.
    private String saleProductDesc;

    //Date added in unix datetime
    private long saleProductDateAdded;

    //Person who added this product (person uid)
    private long saleProductPersonAdded;

    //Picture uid <-> SaleProductPicture 's pk
    private long saleProductPictureUid;

    //If the product active . False is effectively delete
    private boolean saleProductActive;

    //If it is a category it is true. If it is a product it is false (default)
    private boolean saleProductCategory;

    @UmSyncMasterChangeSeqNum
    private long saleProductMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleProductLCSN;

    @UmSyncLastChangedBy
    private int saleProductLCB;

    public SaleProduct(){
        this.saleProductCategory = false;
    }

    public SaleProduct(String name, String decs){
        this.saleProductName = name;
        this.saleProductDesc = decs;
        this.saleProductActive = true;
        this.saleProductCategory = false;

    }

    public long getSaleProductUid() {
        return saleProductUid;
    }

    public void setSaleProductUid(long saleProductUid) {
        this.saleProductUid = saleProductUid;
    }

    public String getSaleProductName() {
        return saleProductName;
    }

    public void setSaleProductName(String saleProductName) {
        this.saleProductName = saleProductName;
    }

    public String getSaleProductDesc() {
        return saleProductDesc;
    }

    public void setSaleProductDesc(String saleProductDesc) {
        this.saleProductDesc = saleProductDesc;
    }

    public long getSaleProductDateAdded() {
        return saleProductDateAdded;
    }

    public void setSaleProductDateAdded(long saleProductDateAdded) {
        this.saleProductDateAdded = saleProductDateAdded;
    }

    public long getSaleProductPersonAdded() {
        return saleProductPersonAdded;
    }

    public void setSaleProductPersonAdded(long saleProductPersonAdded) {
        this.saleProductPersonAdded = saleProductPersonAdded;
    }

    public long getSaleProductPictureUid() {
        return saleProductPictureUid;
    }

    public void setSaleProductPictureUid(long saleProductPictureUid) {
        this.saleProductPictureUid = saleProductPictureUid;
    }

    public boolean isSaleProductActive() {
        return saleProductActive;
    }

    public void setSaleProductActive(boolean saleProductActive) {
        this.saleProductActive = saleProductActive;
    }

    public long getSaleProductMCSN() {
        return saleProductMCSN;
    }

    public void setSaleProductMCSN(long saleProductMCSN) {
        this.saleProductMCSN = saleProductMCSN;
    }

    public long getSaleProductLCSN() {
        return saleProductLCSN;
    }

    public void setSaleProductLCSN(long saleProductLCSN) {
        this.saleProductLCSN = saleProductLCSN;
    }

    public int getSaleProductLCB() {
        return saleProductLCB;
    }

    public void setSaleProductLCB(int saleProductLCB) {
        this.saleProductLCB = saleProductLCB;
    }

    public boolean isSaleProductCategory() {
        return saleProductCategory;
    }

    public void setSaleProductCategory(boolean saleProductCategory) {
        this.saleProductCategory = saleProductCategory;
    }

    public String getSaleProductNameDari() {
        return saleProductNameDari;
    }

    public void setSaleProductNameDari(String saleProductNameDari) {
        this.saleProductNameDari = saleProductNameDari;
    }

    public String getSaleProductDescDari() {
        return saleProductDescDari;
    }

    public void setSaleProductDescDari(String saleProductDescDari) {
        this.saleProductDescDari = saleProductDescDari;
    }

    public String getSaleProductNamePashto() {
        return saleProductNamePashto;
    }

    public void setSaleProductNamePashto(String saleProductNamePashto) {
        this.saleProductNamePashto = saleProductNamePashto;
    }

    public String getSaleProductDescPashto() {
        return saleProductDescPashto;
    }

    public void setSaleProductDescPashto(String saleProductDescPashto) {
        this.saleProductDescPashto = saleProductDescPashto;
    }
}
