package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.DownloadJobItem
import kotlin.js.JsName

interface ContentEntryDetailView : ContentWithOptionsView, ViewWithErrorNotifier, UstadViewWithProgress {

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

    @JsName("updateDownloadProgress")
    fun updateDownloadProgress(progressValue: Float)

    @JsName("setDownloadButtonVisible")
    fun setDownloadButtonVisible(visible: Boolean)

    @JsName("setButtonTextLabel")
    fun setButtonTextLabel(textLabel: String)

    @JsName("showFileOpenWithMimeTypeError")
    fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String)

    @JsName("showEditButton")
    fun showEditButton(show: Boolean)

    @JsName("showFileOpenError")
    fun showFileOpenError(message: String)

    @JsName("updateLocalAvailabilityViews")
    fun updateLocalAvailabilityViews(icon: Int, status: String)

    @JsName("setLocalAvailabilityStatusViewVisible")
    fun setLocalAvailabilityStatusViewVisible(visible: Boolean)

    @JsName("setDownloadProgressVisible")
    fun setDownloadProgressVisible(visible: Boolean)

    @JsName("setDownloadProgressLabel")
    fun setDownloadProgressLabel(progressLabel: String)

    @JsName("setDownloadButtonClickableListener")
    fun setDownloadButtonClickableListener(isDownloadComplete: Boolean)

    @JsName("showDownloadOptionsDialog")
    fun showDownloadOptionsDialog(map: Map<String, String>)

    @JsName("showExportContentIcon")
    fun showExportContentIcon(visible: Boolean)

    companion object {

        const val VIEW_NAME = "ContentEntryDetail"
    }

}
