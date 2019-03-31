package com.ustadmobile.lib.db.entities;

/**
 * Sale 's POJO for representing itself on the view (and recycler views)
 */
public class SaleListDetail {
    String saleTitle;
    String locationName;
    long saleDueDate;
    float saleAmount;
    String saleCurrency;
    long saleCreationDate;
    int saleItemCount;
    boolean preOrder;
    boolean paymentDue;

    public String getSaleTitle() {
        return saleTitle;
    }

    public void setSaleTitle(String saleTitle) {
        this.saleTitle = saleTitle;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getSaleDueDate() {
        return saleDueDate;
    }

    public void setSaleDueDate(long saleDueDate) {
        this.saleDueDate = saleDueDate;
    }

    public float getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(float saleAmount) {
        this.saleAmount = saleAmount;
    }

    public String getSaleCurrency() {
        return saleCurrency;
    }

    public void setSaleCurrency(String saleCurrency) {
        this.saleCurrency = saleCurrency;
    }

    public long getSaleCreationDate() {
        return saleCreationDate;
    }

    public void setSaleCreationDate(long saleCreationDate) {
        this.saleCreationDate = saleCreationDate;
    }

    public int getSaleItemCount() {
        return saleItemCount;
    }

    public void setSaleItemCount(int saleItemCount) {
        this.saleItemCount = saleItemCount;
    }
}
