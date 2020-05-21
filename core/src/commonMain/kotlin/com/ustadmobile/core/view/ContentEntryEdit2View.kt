package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithLanguage> {

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    suspend fun saveContainerOnExit(entryUid: Long,db: UmAppDatabase, repo: UmAppDatabase)

    fun setUpStorageOptions(storageOptions:List<UMStorageDir>)

    fun formatLabel(storage: UMStorageDir): String

    companion object {

        const val VIEW_NAME = "ContentEntryEdit2EditView"

        const val CONTENT_ENTRY_LEAF = "content_entry_leaf"

        const val CONTENT_TYPE = "content_type"

        const val CONTENT_ENTRY_PARENT_UID = "parent_uid"

    }

}