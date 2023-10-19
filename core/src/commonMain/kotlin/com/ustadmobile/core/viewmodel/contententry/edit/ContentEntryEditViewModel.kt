package com.ustadmobile.core.viewmodel.contententry.edit

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.CourseBlock
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI

data class ContentEntryEditUiState(

    val entity: ContentEntryWithBlockAndLanguage? = null,

    val licenceOptions: List<MessageIdOption2> = emptyList(),

    val storageOptions: List<ContainerStorageDir> = emptyList(),

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(),

    val fieldsEnabled: Boolean = true,

    val updateContentVisible: Boolean = false,

    val importError: String? = null,

    val titleError: String? = null,

    val selectedContainerStorageDir: ContainerStorageDir? = null,

    val metadataResult: MetadataResult? = null,

    val compressionEnabled: Boolean = false,

) {
    val contentCompressVisible: Boolean
        get() = metadataResult != null

    val containerStorageOptionVisible: Boolean
        get() = false //entity?.leaf == true

    val courseBlockVisible: Boolean
        get() = entity?.block != null

}

class ContentEntryEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(ContentEntryEditUiState())

    val uiState: Flow<ContentEntryEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this::onClickSave,
                )
            )
        }


        viewModelScope.launch {
            val courseBlockArg = savedStateHandle.getJson(
                key = ARG_COURSEBLOCK,
                deserializer = CourseBlock.serializer(),
            )

            loadEntity(
                serializer = ContentEntryWithBlockAndLanguage.serializer(),
                onLoadFromDb = { db ->
                    db.takeIf { entityUidArg != 0L }?.contentEntryDao
                        ?.findEntryWithBlockAndLanguageByUidAsync(entityUidArg)
                        ?.also {
                            it.block = courseBlockArg
                        }
                },
                makeDefault = {
                    val metadataStr = if(entityUidArg == 0L)
                        savedStateHandle[ARG_IMPORTED_METADATA]
                    else
                        null

                    Napier.d("ContentEntryEditViewModel: entityUidArg=$entityUidArg arg=${savedStateHandle[ARG_IMPORTED_METADATA]}")

                    if(metadataStr != null) {
                        val metadataResult = withContext(Dispatchers.Default) {
                            json.decodeFromString(MetadataResult.serializer(), metadataStr)
                        }

                        //Put the pluginId in the SavedStateHandle - will be required when the user saves
                        savedStateHandle[KEY_PLUGINID] = metadataResult.pluginId.toString()

                        savedStateHandle[KEY_TITLE] = systemImpl.formatString(MR.strings.importing,
                            (metadataResult.displaySourceUrl ?: metadataResult.entry.sourceUrl ?: ""))
                        metadataResult.entry.toEntryWithBlockAndLanguage()
                    }else {
                        ContentEntryWithBlockAndLanguage().apply {
                            contentEntryUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntry.TABLE_ID)
                            block = courseBlockArg
                            leaf = savedStateHandle[ARG_LEAF]?.toBoolean() == true
                        }
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(entity = it)
                    }
                }
            )

            val isLeaf = _uiState.value.entity?.leaf == true
            val savedStateTitle = savedStateHandle[KEY_TITLE]
            val title = when {
                savedStateTitle != null -> savedStateTitle
                entityUidArg == 0L && !isLeaf -> systemImpl.getString(MR.strings.content_editor_create_new_category)
                entityUidArg != 0L && !isLeaf -> systemImpl.getString(MR.strings.edit_folder)
                else -> systemImpl.getString(MR.strings.edit_content)
            }

            _appUiState.update { prev ->
                prev.copy(
                    title = title
                )
            }
        }
    }

    private fun ContentEntry.toEntryWithBlockAndLanguage(): ContentEntryWithBlockAndLanguage {
        return ContentEntryWithBlockAndLanguage().also {
            it.contentEntryUid = contentEntryUid
            it.title = title
            it.description = description
            it.entryId = entryId
            it.author = author
            it.publisher = publisher
            it.licenseType = licenseType
            it.licenseName = licenseName
            it.licenseUrl = licenseUrl
            it.sourceUrl = sourceUrl
            it.lastModified = lastModified
            it.primaryLanguageUid = primaryLanguageUid
            it.languageVariantUid = languageVariantUid
            it.contentFlags = contentFlags
            it.leaf = leaf
            it.publik = publik
            it.ceInactive = ceInactive
            it.contentTypeFlag = contentTypeFlag
            it.contentOwner = contentOwner
            it.contentEntryLocalChangeSeqNum = contentEntryLocalChangeSeqNum
            it.contentEntryMasterChangeSeqNum = contentEntryMasterChangeSeqNum
            it.contentEntryLastChangedBy = contentEntryLastChangedBy
            it.contentEntryLct = contentEntryLct
        }
    }

    fun onContentEntryChanged(
        contentEntry: ContentEntry?
    ) {
        _uiState.update { prev ->
            prev.copy(
                entity = contentEntry?.toEntryWithBlockAndLanguage()?.apply {
                    block = prev.entity?.block
                    language = prev.entity?.language
                }
            )
        }
    }

    fun onClickSave() {
        val contentEntryVal = _uiState.value.entity ?: return

        if(loadingState != LoadingUiState.NOT_LOADING)
            return

        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            val parentUid = savedStateHandle[ARG_PARENT_UID]?.toLong()
            activeDb.withDoorTransactionAsync {
                activeDb.contentEntryDao.upsertAsync(contentEntryVal)

                if(entityUidArg == 0L && parentUid != null) {
                    //create parentchildjoin
                    activeDb.contentEntryParentChildJoinDao.insertAsync(
                        ContentEntryParentChildJoin(
                            cepcjParentContentEntryUid = parentUid,
                            cepcjChildContentEntryUid = contentEntryVal.contentEntryUid,
                        )
                    )
                }
            }

            loadingState = LoadingUiState.NOT_LOADING

            if(entityUidArg == 0L && parentUid != null && !contentEntryVal.leaf) {
                navController.popBackStack(
                    viewName = destinationName,
                    inclusive = true
                )
            }else {
                finishWithResult(contentEntryVal)
            }
        }
    }

    companion object {

        const val ARG_LEAF = "leaf"

        const val ARG_COURSEBLOCK = "courseBlock"

        const val DEST_NAME = "ContentEntryEdit"

        const val ARG_IMPORTED_METADATA = "metadata"

        private const val KEY_PLUGINID = "pluginId"

        //Used to save the title after parsing metadata
        private const val KEY_TITLE = "savedTitle"

    }

}