package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.view.ContentEntryDetailView

expect class ContentEntryDetailPresenter (context: Any, arguments: Map<String, String?>,
                                           view: ContentEntryDetailView,
                                           monitor: LocalAvailabilityMonitor,
                                           statusProvider: DownloadJobItemStatusProvider?,
                                           appRepo: UmAppDatabase)
    :ContentEntryDetailPresenterCommon{

    override fun handleDownloadButtonClick()
}