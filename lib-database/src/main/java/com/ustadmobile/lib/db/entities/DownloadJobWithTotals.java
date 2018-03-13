package com.ustadmobile.lib.db.entities;

/**
 * Created by mike on 3/9/18.
 */

public class DownloadJobWithTotals extends DownloadJob {

    private int numJobItems;

    private long totalDownloadSize;

    public int getNumJobItems() {
        return numJobItems;
    }

    public void setNumJobItems(int numJobItems) {
        this.numJobItems = numJobItems;
    }

    public long getTotalDownloadSize() {
        return totalDownloadSize;
    }

    public void setTotalDownloadSize(long totalDownloadSize) {
        this.totalDownloadSize = totalDownloadSize;
    }
}
