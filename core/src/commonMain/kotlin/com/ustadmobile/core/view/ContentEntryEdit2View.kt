package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithLanguage>{

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    var selectedStorageIndex: Int

    var selectedFileUri: String ?

    var selectedUrl: String?

    var titleErrorEnabled: Boolean

    var fileImportErrorVisible: Boolean

    var storageOptions: List<UMStorageDir> ?

    suspend fun saveContainerOnExit(entryUid: Long,selectedBaseDir: String, db: UmAppDatabase, repo: UmAppDatabase): Container?

    companion object {

        const val VIEW_NAME = "ContentEntryEdit2EditView"

    }

}