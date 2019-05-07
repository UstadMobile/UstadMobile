package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * Combined DownloadJobItem with it's related DownloadSetItem - useful when running a download
 */
class DownloadJobItemWithDownloadSetItem : DownloadJobItem() {

    @UmEmbedded
    var downloadSetItem: DownloadSetItem? = null
}
