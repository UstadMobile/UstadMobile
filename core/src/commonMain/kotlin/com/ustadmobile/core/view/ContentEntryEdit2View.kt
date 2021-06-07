package com.ustadmobile.core.view

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithLanguage>{

    var showCompletionCriteria: Boolean

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    var completionCriteriaOptions: List<ContentEntryEdit2Presenter.CompletionCriteriaMessageIdOption>?

    var selectedStorageIndex: Int

    var titleErrorEnabled: Boolean

    var fileImportErrorVisible: Boolean

    var storageOptions: List<UMStorageDir> ?

    var entryMetaData: ImportedContentEntryMetaData?

    var compressionEnabled: Boolean

    val videoDimensions: Pair<Int, Int>

    var videoUri: String?

    companion object {

        const val VIEW_NAME = "ContentEntryEdit2EditView"

        const val ARG_IMPORTED_METADATA = "metadata"

    }

}