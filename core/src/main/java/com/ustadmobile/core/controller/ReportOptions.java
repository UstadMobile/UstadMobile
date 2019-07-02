package com.ustadmobile.core.controller;

import com.ustadmobile.core.util.UMCalendarUtil;

import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOptionsDetailPresenter.GROUP_BY_LOCATION;

/**
 * Simple POJO of Report Options that will be converted to JSON and vice versa
 */
public class ReportOptions {

    List<Long> productTypes;
    int groupBy;
    boolean showAverage;
    List<Long> les;
    List<Long> locations;
    long fromDate;
    long toDate;
    int fromPrice;
    int toPrice;

    public ReportOptions(){
        //Defaults
        productTypes = new ArrayList<>();
        groupBy = GROUP_BY_LOCATION;
        showAverage = true;
        les = new ArrayList<>();
        locations = new ArrayList<>();
        fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
        toDate = UMCalendarUtil.getDateInMilliPlusDays(0);
        fromPrice = 0;
        toPrice = 75000;
    }
    public List<Long> getProductTypes() {
        return productTypes;
    }

    public void setProductTypes(List<Long> productTypes) {
        this.productTypes = productTypes;
    }

    public int getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(int groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isShowAverage() {
        return showAverage;
    }

    public void setShowAverage(boolean showAverage) {
        this.showAverage = showAverage;
    }

    public List<Long> getLes() {
        return les;
    }

    public void setLes(List<Long> les) {
        this.les = les;
    }

    public List<Long> getLocations() {
        return locations;
    }

    public void setLocations(List<Long> locations) {
        this.locations = locations;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public int getFromPrice() {
        return fromPrice;
    }

    public void setFromPrice(int fromPrice) {
        this.fromPrice = fromPrice;
    }

    public int getToPrice() {
        return toPrice;
    }

    public void setToPrice(int toPrice) {
        this.toPrice = toPrice;
    }
}
