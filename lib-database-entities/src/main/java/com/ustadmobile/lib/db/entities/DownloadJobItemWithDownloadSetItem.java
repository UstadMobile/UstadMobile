package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Combined DownloadJobItem with it's related DownloadSetItem - useful when running a download
 */
public class DownloadJobItemWithDownloadSetItem extends DownloadJobItem {

    @UmEmbedded
    private DownloadSetItem downloadSetItem;


    public DownloadJobItemWithDownloadSetItem() {

    }

    public DownloadJobItemWithDownloadSetItem(DownloadSetItem downloadSetItem, long downloadLength) {
        this.downloadSetItem = downloadSetItem;
        setDjiDsiUid(downloadSetItem.getDsiUid());
        setDownloadLength(downloadLength);
    }

    public DownloadJobItemWithDownloadSetItem(DownloadJobItemWithDownloadSetItem source) {
        setDjiUid(source.getDjiUid());
        setDjiContainerUid(source.getDjiContainerUid());
        setDownloadLength(source.getDownloadLength());
        setDownloadedSoFar(source.getDownloadedSoFar());
        DownloadSetItem dsi = new DownloadSetItem(source.getDjiDsiUid(),
                source.getDownloadSetItem().getDsiContentEntryUid());
        setDownloadSetItem(dsi);
    }



    public DownloadSetItem getDownloadSetItem() {
        return downloadSetItem;
    }

    public void setDownloadSetItem(DownloadSetItem downloadSetItem) {
        this.downloadSetItem = downloadSetItem;
    }
}
