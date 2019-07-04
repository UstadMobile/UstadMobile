package com.ustadmobile.lib.db.entities;

public class ReportSalesLog {

    private String leName;
    private long saleValue;
    private long saleDate;
    private String productNames;
    private String locationName;

    public String getLeName() {
        return leName;
    }

    public void setLeName(String leName) {
        this.leName = leName;
    }

    public long getSaleValue() {
        return saleValue;
    }

    public void setSaleValue(long saleValue) {
        this.saleValue = saleValue;
    }

    public long getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(long saleDate) {
        this.saleDate = saleDate;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
