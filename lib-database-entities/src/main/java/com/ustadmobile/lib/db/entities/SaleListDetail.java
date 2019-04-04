package com.ustadmobile.lib.db.entities;

/**
 * Sale 's POJO for representing itself on the view (and recycler views)
 */
public class SaleListDetail extends Sale {
    String locationName;
    float saleAmount;
    String saleCurrency;
    int saleItemCount;


    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public int getSaleItemCount() {
        return saleItemCount;
    }

    public void setSaleItemCount(int saleItemCount) {
        this.saleItemCount = saleItemCount;
    }
}
