package com.ustadmobile.core.viewmodel.contententry.edit

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.MR
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.setIfNoValueSetYet
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.contententry.getsubtitle.GetSubtitleViewModel
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.ContentEntryAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

data class ContentEntryEditUiState(

    val entity: ContentEntryAndContentJob? = null,

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

    val subtitles: List<SubtitleTrack> = emptyList(),

) {
    val canModifySubtitles: Boolean
        get() = entity?.contentJobItem?.cjiPluginId == ContentImporter.VIDEO_IMPORTER_PLUGIN_ID

}

/**
 * When there is no associated CourseBlock
 *    Show only the ContentEntryEdit part
 *
 * When adding a new ContentEntry as a CourseBlock:
 *   User selects ContentEntry, sees imported data. Then clicks NEXT
 *   Goes to CourseBlockEdit
 *   Clicks DONE
 *
 * When editing a ContentEntry that was added to a CourseBlock
 *   In ClazzEdit, user clicks on the CourseBlock, goes to CourseBlockEdit
 *   User clicks on edit icon next to "Selected Content"
 */
class ContentEntryEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val saveContentEntryUseCase: SaveContentEntryUseCase =
        di.onActiveEndpoint().direct.instance(),
    private val enqueueContentEntryImportUseCase: EnqueueContentEntryImportUseCase =
        di.onActiveEndpoint().direct.instance(),
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        ContentEntryEditUiState()
    )

    val uiState: Flow<ContentEntryEditUiState> = _uiState.asStateFlow()

    private val goToOnContentEntryDone = savedStateHandle[ARG_GO_TO_ON_CONTENT_ENTRY_DONE]?.toInt() ?: 0

    private val goingToCourseBlockEdit = goToOnContentEntryDone == GO_TO_COURSE_BLOCK_EDIT

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
            )
        }

        launchIfHasPermission(
            permissionCheck = { db ->
                goingToCourseBlockEdit ||
                    db.systemPermissionDao().personHasSystemPermission(
                        activeUserPersonUid, PermissionFlags.EDIT_LIBRARY_CONTENT
                    )
            }
        ) {
            loadEntity(
                serializer = ContentEntryAndContentJob.serializer(),
                onLoadFromDb = { db ->
                    //Check if the user can edit the content entry itself...
                    db.takeIf { entityUidArg != 0L }?.contentEntryDao()
                        ?.findByUidWithEditDetails(entityUidArg)?.let { entryAndPicture ->
                            if (entryAndPicture.entry != null) {
                                ContentEntryAndContentJob(
                                    entry = entryAndPicture.entry,
                                    picture = entryAndPicture.picture ?: ContentEntryPicture2(
                                        cepUid = entityUidArg
                                    )
                                )
                            }else {
                                null
                            }
                        }
                },
                makeDefault = {
                    val newContentEntryUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntry.TABLE_ID)
                    val importedMetaData = savedStateHandle.getJson(
                        key = ARG_IMPORTED_METADATA,
                        deserializer = MetadataResult.serializer(),
                    )

                    if(importedMetaData != null) {
                        val pictureUri = importedMetaData.picture?.cepPictureUri
                        if(pictureUri != null) {
                            //Ensure that the
                            savedStateHandle[INIT_PIC_URI] = ""
                        }

                        ContentEntryAndContentJob(
                            entry = importedMetaData.entry.shallowCopy {
                                contentEntryUid = newContentEntryUid
                                if(goingToCourseBlockEdit) {
                                    contentOwnerType = ContentEntry.OWNER_TYPE_COURSE
                                    contentOwner = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0
                                }else {
                                    contentOwnerType = ContentEntry.OWNER_TYPE_LIBRARY
                                    contentOwner = activeUserPersonUid
                                }
                            },
                            contentJobItem = ContentEntryImportJob(
                                cjiPluginId = importedMetaData.importerId,
                                cjiContentEntryUid = newContentEntryUid,
                                sourceUri = importedMetaData.entry.sourceUrl,
                                cjiOriginalFilename = importedMetaData.originalFilename,
                                cjiOwnerPersonUid = activeUserPersonUid,
                            ),
                            picture = importedMetaData.picture?.copy(
                                cepUid = newContentEntryUid,
                            ) ?: ContentEntryPicture2(
                                cepUid = newContentEntryUid,
                            )
                        ).also {
                            savedStateHandle[KEY_TITLE] = systemImpl.formatString(MR.strings.importing,
                                (importedMetaData.originalFilename ?: importedMetaData.entry.sourceUrl ?: ""))
                        }
                    }else {
                        ContentEntryAndContentJob(
                            entry = ContentEntry().apply {
                                contentEntryUid = newContentEntryUid
                                leaf = savedStateHandle[ARG_LEAF]?.toBoolean() == true
                                contentOwner = activeUserPersonUid
                            },
                            picture = ContentEntryPicture2(
                                cepUid = newContentEntryUid,
                            )
                        )
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            entity = it,
                        )
                    }
                }
            ).also {
                savedStateHandle.setIfNoValueSetYet(INIT_PIC_URI, it?.picture?.cepPictureUri ?: "")
            }

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
                            when (goToOnContentEntryDone) {
                                GO_TO_COURSE_BLOCK_EDIT -> MR.strings.next
                                FINISH_WITHOUT_SAVE_TO_DB -> MR.strings.done
                                else -> MR.strings.save
                            }
                        ),
                        onClick = this@ContentEntryEditViewModel::onClickSave,
                    )
                )
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            launch {
                navResultReturner.filteredResultFlowForKey(KEY_HTML_DESCRIPTION).collect {
                    val newDecription =it.result as? String ?: return@collect
                    onContentEntryChanged(
                        _uiState.value.entity?.entry?.shallowCopy {
                            description = newDecription
                        }
                    )
                }
            }
        }
    }

    fun onContentEntryChanged(
        contentEntry: ContentEntry?
    ) {
        val updatedState = _uiState.updateAndGet { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    entry = contentEntry,
                )
            )
        }

        scheduleEntityCommit(updatedState.entity)
    }

    fun onPictureChanged(pictureUri: String?) {
        val updatedState = _uiState.updateAndGet { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    picture = prev.entity.picture?.copy(
                        cepPictureUri = pictureUri,
                    )
                )
            )
        }

        scheduleEntityCommit(updatedState.entity)
    }

    private fun scheduleEntityCommit(entity: ContentEntryAndContentJob?) {
        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = ContentEntryAndContentJob.serializer(),
            commitDelay = 200,
        )
    }

    private fun ContentEntryEditUiState.hasErrors(): Boolean {
        return titleError != null
    }

    fun onEditDescriptionInNewWindow() {
        navigateToEditHtml(
            currentValue = _uiState.value.entity?.entry?.description ?: "",
            resultKey = KEY_HTML_DESCRIPTION,
            title = systemImpl.getString(MR.strings.description),
        )
    }

    fun onSetCompressionLevel(compressionLevel: CompressionLevel) {
        _uiState.update { prev ->
            prev.copy(
                entity = prev.entity?.copy(
                    contentJobItem = prev.entity.contentJobItem?.copy(
                        cjiCompressionLevel = compressionLevel.value
                    )
                )
            )
        }
    }

    fun onSubtitleFileAdded(uri: String, fileName: String) {
        navigateForResult(
            nextViewName = GetSubtitleViewModel.DEST_NAME,
            key = KEY_SUBTITLE,
            currentValue = null,
            serializer = SubtitleTrack.serializer(),
            args = buildMap {
                this[SubtitleEditViewModel.ARG_URI] = uri
                this[SubtitleEditViewModel.ARG_FILENAME] = fileName
            }
        )
    }

    fun onClickSave() {
        val entityVal = _uiState.value.entity

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

        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }

        when {
            /* When a new ContentEntry is being added to a course, then the newly created ContentEntry,
             * associated import job, and courseblock are all passed along to CourseBlockEdit
             * e.g. ClazzEdit -> ContentEntryList -> ContentEntryEdit -> CourseBlockEdit -> return to ClazzEdit
             */
            goToOnContentEntryDone == GO_TO_COURSE_BLOCK_EDIT -> {
                navController.navigate(
                    CourseBlockEditViewModel.DEST_NAME,
                    args = buildMap {
                        this[CourseBlockEditViewModel.ARG_SELECTED_CONTENT_ENTRY] = json.encodeToString(
                            ContentEntryAndContentJob.serializer(), entityVal
                        )

                        putFromSavedStateIfPresent(CourseBlockEditViewModel.COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS)
                    }
                )
            }

            /* When an existing CourseBlock is edited, we return the ContentEntry and any associated
             * import job back to CourseBlockEdit
             * e.g. ClazzEdit -> CourseBlockEdit -> Content Entry Edit -> return to CourseBlockEdit -> return to ClazzEdit
             */
            goToOnContentEntryDone == FINISH_WITHOUT_SAVE_TO_DB -> {
                finishWithResult(entityVal)
            }

            else -> {
                viewModelScope.launch {
                    val parentUidArg = savedStateHandle[ARG_PARENT_UID]?.toLong()
                    saveContentEntryUseCase(
                        contentEntry = contentEntryVal,
                        //Where this is a new ContentEntry (e.g. entityUidArg == 0), it should be
                        // joined to the parentUidArg
                        joinToParentUid = if(entityUidArg == 0L) parentUidArg else null,
                        picture = entityVal.picture,
                        initPictureUri = savedStateHandle[INIT_PIC_URI],
                    )

                    val contentJobItemVal = entityVal.contentJobItem
                    if(contentJobItemVal != null) {
                        enqueueContentEntryImportUseCase(
                            contentJobItem = contentJobItemVal
                        )
                    }

                    val popUpToOnFinish = savedStateHandle[ARG_POPUPTO_ON_FINISH]
                    when {
                        expectedResultDest != null -> {
                            finishWithResult(contentEntryVal)
                        }

                        //Here, we don't go to the detail view. We go back to where the user came from
                        // That is correct for folders. For leaf nodes, maybe should change
                        else -> {
                            navController.popBackStack(
                                viewName = popUpToOnFinish ?: destinationName,
                                inclusive = true
                            )
                        }
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
    }

    companion object {

        const val ARG_LEAF = "leaf"

        const val ARG_COURSEBLOCK = "courseBlock"

        const val DEST_NAME = "ContentEntryEdit"

        const val ARG_IMPORTED_METADATA = "metadata"

        //Used to save the title after parsing metadata
        private const val KEY_TITLE = "savedTitle"

        const val ARG_GO_TO_ON_CONTENT_ENTRY_DONE = "goToOnContentEntryDone"

        const val KEY_HTML_DESCRIPTION = "contentEntryDesc"

        const val KEY_SUBTITLE = "subtitleKey"

        const val GO_TO_COURSE_BLOCK_EDIT = 1

        const val FINISH_WITHOUT_SAVE_TO_DB = 2

    }

}