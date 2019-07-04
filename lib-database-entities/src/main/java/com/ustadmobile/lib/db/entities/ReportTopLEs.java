package com.ustadmobile.lib.db.entities;

public class ReportTopLEs {
    private String leName;
    private long totalSalesValue;
    private String lastActiveOnApp;
    private int leRank;

    public String getLeName() {
        return leName;
    }

    public void setLeName(String leName) {
        this.leName = leName;
    }

    public long getTotalSalesValue() {
        return totalSalesValue;
    }

    public void setTotalSalesValue(long totalSalesValue) {
        this.totalSalesValue = totalSalesValue;
    }

    public String getLastActiveOnApp() {
        return lastActiveOnApp;
    }

    public void setLastActiveOnApp(String lastActiveOnApp) {
        this.lastActiveOnApp = lastActiveOnApp;
    }

    public int getLeRank() {
        return leRank;
    }

    public void setLeRank(int leRank) {
        this.leRank = leRank;
    }
}
