package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 69)
public class SaleProductGroup {

    public static final int PRODUCT_GROUP_TYPE_CATEGORY = 1;
    public static final int PRODUCT_GROUP_TYPE_COLLECTION = 2;
    public static final int PRODUCT_GROUP_TYPE_PRODUCT = 4;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleProductGroupUid;

    //Name of collection. eg: Eid Collection or Toys
    private String saleProductGroupName;

    //Desc eg: This if for eid holidays
    private String saleProductGroupDesc;

    //If collection is to be displayed from one start date
    private long saleProductGroupStartDate;

    //If collection is to be displayed from one start date to one end date
    private long saleProductGroupEndDate;

    //Creation of this collection
    private long saleProductGroupCreationDate;

    private boolean saleProductGroupActive;

    private int saleProductGroupType;

    @UmSyncMasterChangeSeqNum
    private long saleProductGroupMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleProductGroupLCSN;

    @UmSyncLastChangedBy
    private int saleProductGroupLCB;


    public SaleProductGroup(){
        this.saleProductGroupActive = true;
        this.saleProductGroupCreationDate = System.currentTimeMillis();
        this.saleProductGroupType  = PRODUCT_GROUP_TYPE_CATEGORY;
        this.saleProductGroupDesc = "";
    }
    public SaleProductGroup(String name){
        this.saleProductGroupActive = true;
        this.saleProductGroupCreationDate = System.currentTimeMillis();
        this.saleProductGroupType  = PRODUCT_GROUP_TYPE_CATEGORY;
        this.saleProductGroupName = name;
        this.saleProductGroupDesc = "";
    }


    public long getSaleProductGroupUid() {
        return saleProductGroupUid;
    }

    public void setSaleProductGroupUid(long saleProductGroupUid) {
        this.saleProductGroupUid = saleProductGroupUid;
    }

    public String getSaleProductGroupName() {
        return saleProductGroupName;
    }

    public void setSaleProductGroupName(String saleProductGroupName) {
        this.saleProductGroupName = saleProductGroupName;
    }

    public long getSaleProductGroupStartDate() {
        return saleProductGroupStartDate;
    }

    public void setSaleProductGroupStartDate(long saleProductGroupStartDate) {
        this.saleProductGroupStartDate = saleProductGroupStartDate;
    }

    public long getSaleProductGroupEndDate() {
        return saleProductGroupEndDate;
    }

    public void setSaleProductGroupEndDate(long saleProductGroupEndDate) {
        this.saleProductGroupEndDate = saleProductGroupEndDate;
    }

    public long getSaleProductGroupCreationDate() {
        return saleProductGroupCreationDate;
    }

    public void setSaleProductGroupCreationDate(long saleProductGroupCreationDate) {
        this.saleProductGroupCreationDate = saleProductGroupCreationDate;
    }

    public boolean isSaleProductGroupActive() {
        return saleProductGroupActive;
    }

    public void setSaleProductGroupActive(boolean saleProductGroupActive) {
        this.saleProductGroupActive = saleProductGroupActive;
    }

    public int getSaleProductGroupType() {
        return saleProductGroupType;
    }

    public void setSaleProductGroupType(int saleProductGroupType) {
        this.saleProductGroupType = saleProductGroupType;
    }

    public long getSaleProductGroupMCSN() {
        return saleProductGroupMCSN;
    }

    public void setSaleProductGroupMCSN(long saleProductGroupMCSN) {
        this.saleProductGroupMCSN = saleProductGroupMCSN;
    }

    public long getSaleProductGroupLCSN() {
        return saleProductGroupLCSN;
    }

    public void setSaleProductGroupLCSN(long saleProductGroupLCSN) {
        this.saleProductGroupLCSN = saleProductGroupLCSN;
    }

    public int getSaleProductGroupLCB() {
        return saleProductGroupLCB;
    }

    public void setSaleProductGroupLCB(int saleProductGroupLCB) {
        this.saleProductGroupLCB = saleProductGroupLCB;
    }

    public String getSaleProductGroupDesc() {
        return saleProductGroupDesc;
    }

    public void setSaleProductGroupDesc(String saleProductGroupDesc) {
        this.saleProductGroupDesc = saleProductGroupDesc;
    }
}
