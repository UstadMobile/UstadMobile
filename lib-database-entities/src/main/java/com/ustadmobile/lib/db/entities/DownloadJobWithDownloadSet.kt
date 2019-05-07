package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Combined DownloadJob with it's related DownloadSet
 */
class DownloadJobWithDownloadSet : DownloadJob() {

    /**
     * Get the related DownloadSet for this DownloadJob
     *
     * @return the related DownloadSet for this DownloadJob
     */
    /**
     * Set the related DownloadSet for this DownloadJob
     *
     * @param downloadSet the related DownloadSet for this DownloadJob
     */
    @UmEmbedded
    var downloadSet: DownloadSet? = null
}
