package com.ustadmobile.core.view

import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithLanguage>{

    var showCompletionCriteria: Boolean

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    var completionCriteriaOptions: List<ContentEntryEdit2Presenter.CompletionCriteriaMessageIdOption>?

    var selectedStorageIndex: Int

    var titleErrorEnabled: Boolean

    var fileImportErrorVisible: Boolean

    var storageOptions: List<ContainerStorageDir> ?

    var entryMetaData: ImportedContentEntryMetaData?

    var metadataResult: MetadataResult?

    var compressionEnabled: Boolean

    val videoDimensions: Pair<Int, Int>

    var videoUri: String?

    var showUpdateContentButton: Boolean

    companion object {

        const val VIEW_NAME = "ContentEntryEdit2EditView"

        const val ARG_IMPORTED_METADATA = "metadata"

        const val ARG_URI = "uri"

    }

}