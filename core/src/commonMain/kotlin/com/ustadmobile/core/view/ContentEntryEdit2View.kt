package com.ustadmobile.core.view

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.lib.db.entities.ContentEntryPicture
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage


interface ContentEntryEdit2View: UstadEditView<ContentEntryWithBlockAndLanguage>{

    var contentEntryPicture: ContentEntryPicture?

    var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>?

    var completionCriteriaOptions: List<ContentEntryEdit2Presenter.CompletionCriteriaMessageIdOption>?

    var selectedStorageIndex: Int

    var titleErrorEnabled: Boolean

    var fileImportErrorVisible: Boolean

    var storageOptions: List<ContainerStorageDir> ?

    var metadataResult: MetadataResult?

    var compressionEnabled: Boolean

    val videoDimensions: Pair<Int, Int>

    var videoUri: String?

    var showUpdateContentButton: Boolean

    var caGracePeriodError: String?
    var caDeadlineError: String?
    var caStartDateError: String?
    var caMaxPointsError: String?

    var startDate: Long
    var startTime: Long

    var deadlineDate: Long
    var deadlineTime: Long

    var gracePeriodDate: Long
    var gracePeriodTime: Long

    var timeZone: String?


    companion object {

        const val VIEW_NAME = "ContentEntryEditView"

        const val ARG_IMPORTED_METADATA = "metadata"

        const val ARG_URI = "uri"

        const val BLOCK_REQUIRED = "isBlockRequired"

    }

}