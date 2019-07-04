package com.ustadmobile.lib.db.entities;

public class ReportSalesPerformance {

    long saleAmount;
    String locationName;
    long locationUid;
    long saleUid;
    String saleCreationDate;
    String dateGroup;
    String firstDateOccurence;
    String saleProductName;
    int saleItemQuantity;
    String producerName;
    long producerUid;
    String leName;
    long leUid;
    String productTypeName;
    long productTypeUid;
    String grantee;

    public long getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(long saleAmount) {
        this.saleAmount = saleAmount;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getLocationUid() {
        return locationUid;
    }

    public void setLocationUid(long locationUid) {
        this.locationUid = locationUid;
    }

    public long getSaleUid() {
        return saleUid;
    }

    public void setSaleUid(long saleUid) {
        this.saleUid = saleUid;
    }

    public String getSaleCreationDate() {
        return saleCreationDate;
    }

    public void setSaleCreationDate(String saleCreationDate) {
        this.saleCreationDate = saleCreationDate;
    }

    public String getDateGroup() {
        return dateGroup;
    }

    public void setDateGroup(String dateGroup) {
        this.dateGroup = dateGroup;
    }

    public String getFirstDateOccurence() {
        return firstDateOccurence;
    }

    public void setFirstDateOccurence(String firstDateOccurence) {
        this.firstDateOccurence = firstDateOccurence;
    }

    public String getSaleProductName() {
        return saleProductName;
    }

    public void setSaleProductName(String saleProductName) {
        this.saleProductName = saleProductName;
    }

    public int getSaleItemQuantity() {
        return saleItemQuantity;
    }

    public void setSaleItemQuantity(int saleItemQuantity) {
        this.saleItemQuantity = saleItemQuantity;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public long getProducerUid() {
        return producerUid;
    }

    public void setProducerUid(long producerUid) {
        this.producerUid = producerUid;
    }

    public String getLeName() {
        return leName;
    }

    public void setLeName(String leName) {
        this.leName = leName;
    }

    public long getLeUid() {
        return leUid;
    }

    public void setLeUid(long leUid) {
        this.leUid = leUid;
    }

    public String getProductTypeName() {
        return productTypeName;
    }

    public void setProductTypeName(String productTypeName) {
        this.productTypeName = productTypeName;
    }

    public long getProductTypeUid() {
        return productTypeUid;
    }

    public void setProductTypeUid(long productTypeUid) {
        this.productTypeUid = productTypeUid;
    }

    public String getGrantee() {
        return grantee;
    }

    public void setGrantee(String grantee) {
        this.grantee = grantee;
    }
}
