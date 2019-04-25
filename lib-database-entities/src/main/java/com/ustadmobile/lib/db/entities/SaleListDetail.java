package com.ustadmobile.lib.db.entities;

/**
 * Sale 's POJO for representing itself on the view (and recycler views)
 */
public class SaleListDetail extends Sale {

    String locationName;
    float saleAmount;
    String saleCurrency;
    int saleItemCount;
    String saleTitleGen;
    float saleAmountPaid;
    float saleAmountDue;
    long earliestDueDate;


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

    public String getSaleTitleGen() {
        return saleTitleGen;
    }

    public void setSaleTitleGen(String saleTitleGen) {
        this.saleTitleGen = saleTitleGen;
    }

    public float getSaleAmountPaid() {
        return saleAmountPaid;
    }

    public void setSaleAmountPaid(float saleAmountPaid) {
        this.saleAmountPaid = saleAmountPaid;
    }

    public float getSaleAmountDue() {
        return saleAmountDue;
    }

    public void setSaleAmountDue(float saleAmountDue) {
        this.saleAmountDue = saleAmountDue;
    }

    public long getEarliestDueDate() {
        return earliestDueDate;
    }

    public void setEarliestDueDate(long earliestDueDate) {
        this.earliestDueDate = earliestDueDate;
    }
}
