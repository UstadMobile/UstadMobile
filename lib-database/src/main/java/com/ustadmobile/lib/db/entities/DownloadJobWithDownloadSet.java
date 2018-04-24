package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Combined DownloadJob with it's related DownloadSet
 */
public class DownloadJobWithDownloadSet extends DownloadJob {

    @UmEmbedded
    private DownloadSet downloadSet;

    /**
     * Get the related DownloadSet for this DownloadJob
     *
     * @return the related DownloadSet for this DownloadJob
     */
    public DownloadSet getDownloadSet() {
        return downloadSet;
    }

    /**
     * Set the related DownloadSet for this DownloadJob
     *
     * @param downloadSet the related DownloadSet for this DownloadJob
     */
    public void setDownloadSet(DownloadSet downloadSet) {
        this.downloadSet = downloadSet;
    }
}
