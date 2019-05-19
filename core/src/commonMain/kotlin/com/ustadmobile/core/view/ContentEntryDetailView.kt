package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage

interface ContentEntryDetailView : UstadView, ViewWithErrorNotifier {

    val allKnowAvailabilityStatus: Set<Long>


    fun setContentEntryTitle(title: String)

    fun setContentEntryDesc(desc: String)

    fun setContentEntryLicense(license: String)

    fun setContentEntryAuthor(author: String)

    fun setDetailsButtonEnabled(enabled: Boolean)

    fun setDownloadSize(fileSize: Long)

    fun loadEntryDetailsThumbnail(thumbnailUrl: String)

    fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>, entryUuid: Long)

    fun updateDownloadProgress(progressValue: Float)

    fun setDownloadButtonVisible(visible: Boolean)

    fun setButtonTextLabel(textLabel: String)

    fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String)

    fun showFileOpenError(message: String)

    fun updateLocalAvailabilityViews(icon: Int, status: String)

    fun setLocalAvailabilityStatusViewVisible(visible: Boolean)

    fun setTranslationLabelVisible(visible: Boolean)

    fun setFlexBoxVisible(visible: Boolean)

    fun setDownloadProgressVisible(visible: Boolean)

    fun setDownloadProgressLabel(progressLabel: String)

    fun setDownloadButtonClickableListener(isDownloadComplete: Boolean)

    fun showDownloadOptionsDialog(hashtable: HashMap<String, String>)

    companion object {

        const val VIEW_NAME = "ContentEntryDetail"
    }

}
