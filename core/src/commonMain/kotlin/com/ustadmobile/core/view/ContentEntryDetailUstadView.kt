package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.DownloadJobItem
import kotlin.js.JsName

interface ContentEntryDetailView : UstadViewWithSnackBar, UstadViewWithProgress {

    @JsName("setContentEntry")
    fun setContentEntry(contentEntry: ContentEntry)

    @JsName("setDownloadJobItemStatus")
    fun setDownloadJobItemStatus(downloadJobItem: DownloadJobItem?)

    @JsName("setContentEntryLicense")
    fun setContentEntryLicense(license: String)

    @JsName("setMainButtonEnabled")
    fun setMainButtonEnabled(enabled: Boolean)

    @JsName("setDownloadSize")
    fun setDownloadSize(fileSize: Long)

    @JsName("setAvailableTranslations")
    fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>)

    @JsName("showFileOpenWithMimeTypeError")
    fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String)

    @JsName("setEditButtonVisible")
    fun setEditButtonVisible(show: Boolean)

    @JsName("showFileOpenError")
    fun showFileOpenError(message: String)

    @JsName("updateLocalAvailabilityViews")
    fun updateLocalAvailabilityViews(icon: Int, status: String)

    @JsName("setLocalAvailabilityStatusViewVisible")
    fun setLocalAvailabilityStatusViewVisible(visible: Boolean)

    @JsName("showDownloadOptionsDialog")
    fun showDownloadOptionsDialog(map: Map<String, String>)

    @JsName("setExportContentIconVisible")
    fun setExportContentIconVisible(visible: Boolean)

    companion object {

        const val VIEW_NAME = "ContentEntryDetail"
    }

}
