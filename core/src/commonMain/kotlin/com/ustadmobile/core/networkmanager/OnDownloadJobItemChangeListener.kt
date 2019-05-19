package com.ustadmobile.core.networkmanager

import com.ustadmobile.lib.db.entities.DownloadJobItemStatus

interface OnDownloadJobItemChangeListener {

    fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int)

}
