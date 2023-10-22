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
import com.ustadmobile.lib.db.composites.ContentEntryBlockLanguageAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentJobItem
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

    val entity: ContentEntryBlockLanguageAndContentJob? = null,

    val licenceOptions: List<MessageIdOption2> = emptyList(),

    val storageOptions: List<ContainerStorageDir> = emptyList(),

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(),

    val fieldsEnabled: Boolean = false,

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
        get() = false

    val courseBlockVisible: Boolean
        get() = entity?.block != null

}

/**
 * When there is no associated CourseBlock
 *    Show only the ContentEntryEdit part
 *
 * When there is an associated CourseBlock and the user has permission to edit the ContentEntry itself:
 *    The title and description will be shown only once (e.g. as part of CourseBlockEdit). The title
 *    and description will be copied from CourseBlock to ContentEntry
 *
 * When there is an associated CourseBlock and the user does not have permission to edit the ContentEntry itself:
 *    Only the CourseBlock part of the screen will be displayed. ContentEntry specific fields (e.g.
 *    license, author, etc) will not be displayed. This would probably be the case if a teacher
 *    selects a content item from the library.
 */
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

            _uiState.takeIf { courseBlockArg != null }?.update { prev ->
                prev.copy(
                    entity = prev.entity?.copy(
                        block = courseBlockArg
                    ) ?: ContentEntryBlockLanguageAndContentJob(block = courseBlockArg)
                )
            }

            loadEntity(
                serializer = ContentEntryBlockLanguageAndContentJob.serializer(),
                onLoadFromDb = { db ->
                    //Check if the user can edit the content entry itself...
                    db.takeIf { entityUidArg != 0L }?.contentEntryDao
                        ?.findEntryWithLanguageByEntryIdAsync(entityUidArg)
                        ?.toContentEntryAndBlock(courseBlockArg)
                },
                makeDefault = {
                    val importedMetaData = savedStateHandle.getJson(
                        key = ARG_IMPORTED_METADATA,
                        deserializer = MetadataResult.serializer(),
                    )

                    if(importedMetaData != null) {
                        ContentEntryBlockLanguageAndContentJob(
                            entry = importedMetaData.entry,
                            block = courseBlockArg,
                            contentJobItem = ContentJobItem(
                                cjiPluginId = importedMetaData.pluginId,
                            )
                        ).also {
                            savedStateHandle[KEY_TITLE] = systemImpl.formatString(MR.strings.importing,
                                (importedMetaData.displaySourceUrl ?: importedMetaData.entry.sourceUrl ?: ""))
                        }
                    }else {
                        ContentEntryBlockLanguageAndContentJob(
                            entry = ContentEntry().apply {
                                contentEntryUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntry.TABLE_ID)
                                leaf = savedStateHandle[ARG_LEAF]?.toBoolean() == true
                            },
                            block = courseBlockArg,
                        )
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(entity = it)
                    }
                }
            )

            val isLeaf = _uiState.value.entity?.entry?.leaf == true
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

    fun onContentEntryChanged(
        contentEntry: ContentEntry?
    ) {
        _uiState.update { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    entry = contentEntry,
                )
            )
        }
    }

    fun onClickSave() {
        val contentEntryVal = _uiState.value.entity?.entry ?: return

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