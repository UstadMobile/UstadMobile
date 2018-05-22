package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Combined DownloadJobItem with it's related DownloadSetItem - useful when running a download
 */
public class DownloadJobItemWithDownloadSetItem extends DownloadJobItem {

    public DownloadJobItemWithDownloadSetItem(int downloadSetItemId, int downloadJobId) {
        super(downloadSetItemId, downloadJobId);
    }

    @UmEmbedded
    private DownloadSetItem downloadSetItem;

    public DownloadSetItem getDownloadSetItem() {
        return downloadSetItem;
    }

    public void setDownloadSetItem(DownloadSetItem downloadSetItem) {
        this.downloadSetItem = downloadSetItem;
    }
}
