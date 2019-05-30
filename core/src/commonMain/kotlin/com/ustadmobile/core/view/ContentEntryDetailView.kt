package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import kotlin.js.JsName

interface ContentEntryDetailView : UstadView, ViewWithErrorNotifier {

    val allKnowAvailabilityStatus: Set<Long>


    @JsName("setContentEntryTitle")
    fun setContentEntryTitle(title: String)

    @JsName("setContentEntryDesc")
    fun setContentEntryDesc(desc: String)

    @JsName("setContentEntryLicense")
    fun setContentEntryLicense(license: String)

    @JsName("setContentEntryAuthor")
    fun setContentEntryAuthor(author: String)

    @JsName("setDetailsButtonEnabled")
    fun setDetailsButtonEnabled(enabled: Boolean)

    @JsName("setDownloadSize")
    fun setDownloadSize(fileSize: Long)

    @JsName("loadEntryDetailsThumbnail")
    fun loadEntryDetailsThumbnail(thumbnailUrl: String)

    @JsName("setAvailableTranslations")
    fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>, entryUuid: Long)

    @JsName("updateDownloadProgress")
    fun updateDownloadProgress(progressValue: Float)

    @JsName("setDownloadButtonVisible")
    fun setDownloadButtonVisible(visible: Boolean)

    @JsName("setButtonTextLabel")
    fun setButtonTextLabel(textLabel: String)

    @JsName("showFileOpenWithMimeTypeError")
    fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String)

    @JsName("showFileOpenError")
    fun showFileOpenError(message: String)

    @JsName("updateLocalAvailabilityViews")
    fun updateLocalAvailabilityViews(icon: Int, status: String)

    @JsName("setLocalAvailabilityStatusViewVisible")
    fun setLocalAvailabilityStatusViewVisible(visible: Boolean)

    @JsName("setTranslationLabelVisible")
    fun setTranslationLabelVisible(visible: Boolean)

    @JsName("setFlexBoxVisible")
    fun setFlexBoxVisible(visible: Boolean)

    @JsName("setDownloadProgressVisible")
    fun setDownloadProgressVisible(visible: Boolean)

    @JsName("setDownloadProgressLabel")
    fun setDownloadProgressLabel(progressLabel: String)

    @JsName("setDownloadButtonClickableListener")
    fun setDownloadButtonClickableListener(isDownloadComplete: Boolean)

    @JsName("showDownloadOptionsDialog")
    fun showDownloadOptionsDialog(hashtable: HashMap<String, String>)

    companion object {

        const val VIEW_NAME = "ContentEntryDetail"
    }

}
