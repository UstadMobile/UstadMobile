package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.lib.db.entities.ContentEntry

/**
 * @author kileha3
 */

interface ContentEntryEditView : UstadView {

    fun showFileSelector(show: Boolean)

    fun showErrorMessage(message: String?, visible: Boolean)

    fun showAddThumbnailMessage()

    fun showStorageOptions(visible: Boolean)

    fun showImageSelector(visible: Boolean)

    fun showMessageAndDismissDialog(message: String?, document: Boolean)


    fun setContentEntry(contentEntry: ContentEntry)

    fun updateFileBtnLabel(label: String)

    fun setUpLicence(licence: List<String>, index: Int)

    fun setUpStorageOption(storageDirs: List<UMStorageDir>)

    fun showProgressDialog()

    fun dismissDialog()

    fun startBrowseFiles()

    fun showUpdateContentDialog(title: String, options: List<String>)

    companion object{

        const val CONTENT_ENTRY_LEAF = "content_entry_leaf"

        const val CONTENT_TYPE = "content_type"

        const val VIEW_NAME = "EntryFragment"
    }
}
