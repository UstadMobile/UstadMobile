package com.ustadmobile.core.viewmodel.contententry.edit

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.ContentEntryBlockLanguageAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

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

    //Where a CourseBlock is being edited, then the title/description will be taken from the
    //CourseBlock
    val contentEntryTitleVisible: Boolean
        get() = !courseBlockVisible

    val contentEntryDescriptionVisible: Boolean
        get() = !courseBlockVisible

    fun copyWithFieldsEnabled(fieldsEnabled: Boolean) : ContentEntryEditUiState{
        return copy(
            fieldsEnabled = fieldsEnabled,
            courseBlockEditUiState = courseBlockEditUiState.copy(
                fieldsEnabled = fieldsEnabled
            )
        )
    }

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
    private val saveContentEntryUseCase: SaveContentEntryUseCase = di.onActiveEndpoint().direct.instance(),
    private val importContentUseCase: ImportContentUseCase = di.onActiveEndpoint().direct.instance(),
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(ContentEntryEditUiState())

    val uiState: Flow<ContentEntryEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
            )
        }


        viewModelScope.launch {
            val courseBlockArgVal = savedStateHandle.getJson(
                key = ARG_COURSEBLOCK,
                deserializer = CourseBlock.serializer(),
            )

            loadEntity(
                serializer = ContentEntryBlockLanguageAndContentJob.serializer(),
                onLoadFromDb = { db ->
                    //Check if the user can edit the content entry itself...
                    db.takeIf { entityUidArg != 0L }?.contentEntryDao
                        ?.findEntryWithLanguageByEntryIdAsync(entityUidArg)
                        ?.toContentEntryAndBlock(courseBlockArgVal)
                },
                makeDefault = {
                    val newContentEntryUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntry.TABLE_ID)
                    val importedMetaData = savedStateHandle.getJson(
                        key = ARG_IMPORTED_METADATA,
                        deserializer = MetadataResult.serializer(),
                    )

                    if(importedMetaData != null) {
                        ContentEntryBlockLanguageAndContentJob(
                            entry = importedMetaData.entry.shallowCopy {
                                contentEntryUid = newContentEntryUid
                                contentOwner = activeUserPersonUid
                            },
                            block = courseBlockArgVal?.shallowCopy {
                                cbTitle = importedMetaData.entry.title
                                cbDescription = importedMetaData.entry.description
                            },
                            contentJobItem = ContentJobItem(
                                cjiPluginId = importedMetaData.importerId,
                                cjiContentEntryUid = newContentEntryUid,
                                sourceUri = importedMetaData.entry.sourceUrl,
                                cjiOriginalFilename = importedMetaData.originalFilename,
                            ),
                            contentJob = ContentJob()
                        ).also {
                            savedStateHandle[KEY_TITLE] = systemImpl.formatString(MR.strings.importing,
                                (importedMetaData.originalFilename ?: importedMetaData.entry.sourceUrl ?: ""))
                        }
                    }else {
                        ContentEntryBlockLanguageAndContentJob(
                            entry = ContentEntry().apply {
                                contentEntryUid = newContentEntryUid
                                leaf = savedStateHandle[ARG_LEAF]?.toBoolean() == true
                                contentOwner = activeUserPersonUid
                            },
                            block = courseBlockArgVal,
                        )
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            entity = it,
                            courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                                courseBlock = it?.block
                            )
                        )
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
                    title = title,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(
                            //If the CourseBlock arg is provided, then the actual save to db is
                            // done by ClazzEdit, not here
                            if(_uiState.value.entity?.block != null)
                                MR.strings.done
                            else
                                MR.strings.save
                        ),
                        onClick = this@ContentEntryEditViewModel::onClickSave,
                    )
                )
            }

            _uiState.update { prev ->
                prev.copyWithFieldsEnabled(true)
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

    private fun ContentEntryEditUiState.hasErrors(): Boolean {
        return titleError != null
    }

    fun onCourseBlockChanged(
        courseBlock: CourseBlock?
    ) {
        _uiState.update { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    block = courseBlock,
                ),
                courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                    courseBlock = courseBlock
                )
            )
        }
    }

    fun onClickSave() {
        val entityVal = _uiState.value.entity?.copy(
            block = _uiState.value.entity?.block?.shallowCopy {
                //Make sure that the CourseBlock (if provided) has the cbEntityUid and cbType set correctly
                cbType = CourseBlock.BLOCK_CONTENT_TYPE
                cbEntityUid = _uiState.value.entity?.entry?.contentEntryUid ?: 0L
            }
        )

        val contentEntry = _uiState.value.entity?.entry
        _uiState.update { prev ->
            prev.copy(
                titleError = if(contentEntry?.title.isNullOrBlank()) systemImpl.getString(MR.strings.required) else null
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = true
                )
            }

            return
        }

        val contentEntryVal = entityVal?.entry ?: return

        if(!_uiState.value.fieldsEnabled) {
            return
        }

        _uiState.update { prev -> prev.copyWithFieldsEnabled(false) }

        if(entityVal.block != null) {
            //This is being edited as part of a course. We won't save anything here. Saving will
            //be done in ClazzEditViewModel
            finishWithResult(entityVal)
        }else {
            viewModelScope.launch {
                val parentUidArg = savedStateHandle[ARG_PARENT_UID]?.toLong()
                saveContentEntryUseCase(
                    contentEntry = contentEntryVal,
                    //Where this is a new ContentEntry (e.g. entityUidArg == 0), it should be
                    // joined to the parentUidArg
                    joinToParentUid = if(entityUidArg == 0L) parentUidArg else null
                )

                val contentJobItemVal = entityVal.contentJobItem
                val contentJobVal = entityVal.contentJob
                if(contentJobVal != null && contentJobItemVal != null) {
                    importContentUseCase(
                        contentJob = contentJobVal,
                        contentJobItem = contentJobItemVal
                    )
                }

                //if a new folder was created, don't go to the "detail view"
                if(entityUidArg == 0L && parentUidArg != null && !contentEntryVal.leaf) {
                    navController.popBackStack(
                        viewName = destinationName,
                        inclusive = true
                    )
                }else {
                    finishWithResult(contentEntryVal)
                }

                if (_uiState.value.hasErrors()) {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev ->
                        prev.copy(
                            fieldsEnabled = true
                        )
                    }

                    return@launch
                }
            }
        }
    }

    companion object {

        const val ARG_LEAF = "leaf"

        const val ARG_COURSEBLOCK = "courseBlock"

        const val DEST_NAME = "ContentEntryEdit"

        const val ARG_IMPORTED_METADATA = "metadata"

        //Used to save the title after parsing metadata
        private const val KEY_TITLE = "savedTitle"

    }

}