package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage

data class ContentEntryEditUiState(

    val entity: ContentEntryWithBlockAndLanguage? = null,

    val licenceOptions: List<MessageIdOption2> = emptyList(),

    val storageOptions: List<ContainerStorageDir> = emptyList(),

    val courseBlockEditUiState: CourseBlockEditUiState? = null,

    val fieldsEnabled: Boolean = true,

    val updateContentVisible: Boolean = false,

    val importError: String? = null,

    val titleError: String? = null,

    var metadataResult: MetadataResult? = null

) {
    val contentCompressVisible: Boolean
        get() = metadataResult != null

    val containerStorageOptionVisible: Boolean
        get() = entity?.leaf == true
}