package com.ustadmobile.core.view

interface ContentEntryImportLinkView : UstadView, UstadViewWithProgress {

    fun showUrlStatus(isValid: Boolean, message: String)

    fun displayUrl(url: String)

    fun returnResult()

    fun showHideVideoTitle(showTitle: Boolean)

    fun showNoTitleEntered(errorText: String)

    fun checkDoneButton()

    fun enableDisableEditText(enable: Boolean)

    fun showHideErrorMessage(show: Boolean)

    fun updateSourceUrl(sourceUrl: String)

    companion object {

        const val CONTENT_ENTRY_PARENT_UID = "ContentEntryParentUid"

        const val CONTENT_ENTRY_UID = "ContentEntryUid"

        const val VIEW_NAME = "ContentEntryImportLinkView"

        const val END_POINT_URL = "endpointUrl"

    }

}