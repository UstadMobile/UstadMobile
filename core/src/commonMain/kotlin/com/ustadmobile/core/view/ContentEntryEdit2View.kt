package com.ustadmobile.core.view

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithLanguage>{

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    var selectedStorageIndex: Int

    var titleErrorEnabled: Boolean

    var fileImportErrorVisible: Boolean

    var storageOptions: List<UMStorageDir> ?

    var entryMetaData: ImportedContentEntryMetaData?

    var compressionEnabled: Boolean

    val videoDimensions: Pair<Int, Int>

    var videoUri: String?

    companion object {

        const val VIEW_NAME = "ContentEntryEditView"

        const val ARG_IMPORTED_METADATA = "metadata"

        const val ARG_URI = "uri"

    }

}