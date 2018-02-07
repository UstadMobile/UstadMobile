package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmRelation;

import java.util.List;

/**
 * Created by mike on 1/26/18.
 */

public class DownloadJobWithRelations extends DownloadJob{

    @UmRelation(parentColumn = "id", entityColumn = "downloadJobId")
    private List <DownloadJobItem> downloadJobItems;

    public List<DownloadJobItem> getDownloadJobItems() {
        return downloadJobItems;
    }

    public void setDownloadJobItems(List<DownloadJobItem> downloadJobItems) {
        this.downloadJobItems = downloadJobItems;
    }

    public DownloadJobItem getJobItemByEntryId(String entryId){
        if(downloadJobItems == null)
            return null;

        for(DownloadJobItem item : downloadJobItems) {
            if (entryId.equals(item.getEntryId()))
                return item;
        }

        return null;
    }

}
