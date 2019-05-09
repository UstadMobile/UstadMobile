package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Embedded
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
    @Embedded
    var downloadSet: DownloadSet? = null
}
