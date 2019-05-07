package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Combined DownloadJobItem with it's related DownloadSetItem - useful when running a download
 */
class DownloadJobItemWithDownloadSetItem : DownloadJobItem() {

    @UmEmbedded
    @Embedded
    var downloadSetItem: DownloadSetItem? = null
}
