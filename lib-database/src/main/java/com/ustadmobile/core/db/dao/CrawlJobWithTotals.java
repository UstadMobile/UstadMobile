package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.db.entities.CrawlJob;

/**
 * Created by mike on 3/6/18.
 */

public class CrawlJobWithTotals extends CrawlJob{

    private int numItems;

    private int numItemsCompleted;

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }

    public int getNumItemsCompleted() {
        return numItemsCompleted;
    }

    public void setNumItemsCompleted(int numItemsCompleted) {
        this.numItemsCompleted = numItemsCompleted;
    }
}
