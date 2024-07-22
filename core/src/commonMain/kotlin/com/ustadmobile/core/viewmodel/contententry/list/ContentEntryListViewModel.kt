package com.ustadmobile.core.viewmodel.contententry.list

import com.ustadmobile.core.db.dao.ContentEntryDaoCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel.Companion.FILTER_BY_PARENT_UID
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.contententry.delete.DeleteContentEntryParentChildJoinUseCase
import com.ustadmobile.core.domain.contententry.move.MoveContentEntriesUseCase
import com.ustadmobile.core.domain.export.ExportContentEntryUstadZipUseCase
import com.ustadmobile.core.domain.export.ExportProgress
import com.ustadmobile.core.impl.appstate.AppActionButton
import com.ustadmobile.core.impl.appstate.AppBarColors
import com.ustadmobile.core.impl.appstate.AppStateIcon
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.lib.db.composites.ContentEntryAndContentJob
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ContentEntry
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.instance

data class ContentEntryListUiState(
    val exportProgress: ExportProgress? = null,
    val filterMode: Int = FILTER_BY_PARENT_UID,

    val contentEntryList: ListPagingSourceFactory<ContentEntryAndListDetail> = { EmptyPagingSource() },

    val selectedChipId: Int = FILTER_BY_PARENT_UID,

    val filterOptions: List<MessageIdOption2> = listOf(),

    val showHiddenEntries: Boolean = false,

    val onlyFolderFilter: Boolean = false,

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OPTIONS,

    val activeSortOption: SortOrderOption = sortOptions.first(),

    val createNewFolderItemVisible: Boolean = false,

    val importFromLinkItemVisible: Boolean = false,

    val importFromFileItemVisible: Boolean = false,

    val createNewOptionsVisible: Boolean = false,

    val selectedEntries: Set<ContentEntryListSelectedItem> = emptySet(),

    val showSelectFolderButton: Boolean = false,

    val contextMenuItems: (ContentEntryAndListDetail) -> List<UstadContextMenuItem> = { emptyList() },

    val hasWritePermission: Boolean = false,

    ) {

    val showChips: Boolean
        get() =filterOptions.isNotEmpty()

    val selectedEntryUids: Set<Long> = selectedEntries.map {
        it.contentEntryUid
    }.toSet()

    companion object {

        val DEFAULT_SORT_OPTIONS = listOf(
            SortOrderOption(MR.strings.title, ContentEntryDaoCommon.SORT_TITLE_ASC, true),
            SortOrderOption(MR.strings.title, ContentEntryDaoCommon.SORT_TITLE_DESC, false),
        )

    }
}

@Serializable
data class ContentEntryListSelectedItem(
    val contentEntryUid: Long,
    val contentEntryParentChildJoinUid: Long,
    val parentContentEntryUid: Long,
)

fun ContentEntryAndListDetail.asSelectedItem() = ContentEntryListSelectedItem(
    contentEntryUid = contentEntry?.contentEntryUid ?: 0,
    contentEntryParentChildJoinUid = contentEntryParentChildJoin?.cepcjUid ?: 0,
    parentContentEntryUid = contentEntryParentChildJoin?.cepcjParentContentEntryUid ?: 0,
)

/**
 * Shows a list of ContentEntry entities. This is used to show a list of items in library folders
 * and to allow users to pick a ContentEntry to add to a course.
 *
 * Moving content entries flow:
 *  1) On ContentEntryList, the user can select one or more items to be moved. This is handled
 *     by onSetSelected.
 *  2) When the user clicks the action bar button to move the selection, we navigate to this same
 *     screen using an alias destination name ( DEST_NAME_PICKER ) to avoid problems returning the
 *     result (if we didn't change the destination name, then if the user was going through folders,
 *     navigation would not popup to the actual destination). This sets the ARG_SELECT_FOLDER_MODE
 *     to true.
 *  3) When the user selects a folder onClickSelectThisFolder returns the current folder
 *  4) The destination that initiated the navigation runs the repository update query to move
 *     entries.
 */
class ContentEntryListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String,
): UstadListViewModel<ContentEntryListUiState>(
    di, savedStateHandle, ContentEntryListUiState(), destName
) {

    private val parentEntryUid: Long = savedStateHandle[ARG_PARENT_UID]?.toLong()
        ?: LIBRARY_ROOT_CONTENT_ENTRY_UID

    /**
     * This will be true when the user is selecting content as part of selecting it from ClazzEdit
     */
    private val hasCourseBlockArg: Boolean =
        savedStateHandle[ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE]?.toInt() == ContentEntryEditViewModel.GO_TO_COURSE_BLOCK_EDIT

    private val selectFolderMode: Boolean = savedStateHandle[ARG_SELECT_FOLDER_MODE]?.toBoolean() ?: false

    private val pagingSourceFactory: ListPagingSourceFactory<ContentEntryAndListDetail> = {
        when(_uiState.value.selectedChipId) {
            FILTER_MY_CONTENT -> activeRepo.contentEntryDao().getContentByOwner(
                activeUserPersonUid
            )

            FILTER_FROM_MY_COURSES -> activeRepo.contentEntryDao().getContentFromMyCourses(
                activeUserPersonUid
            )

            FILTER_FROM_LIBRARY -> {
                activeRepo.contentEntryDao().getChildrenByParentUidWithCategoryFilterOrderByName(
                    accountPersonUid = activeUserPersonUid,
                    parentUid = parentEntryUid,
                    langParam = 0,
                    categoryParam0 = 0,
                    sortOrder = _uiState.value.activeSortOption.flag,
                    includeDeleted = false,
                )
            }

            FILTER_BY_PARENT_UID -> {
                activeRepo.contentEntryDao().getChildrenByParentUidWithCategoryFilterOrderByName(
                    accountPersonUid = activeUserPersonUid,
                    parentUid = parentEntryUid,
                    langParam = 0,
                    categoryParam0 = 0,
                    sortOrder = _uiState.value.activeSortOption.flag,
                    includeDeleted = false,
                )
            }

            else -> EmptyPagingSource()
        }
    }


    private var defaultTitle: String = ""

    /**
     * This is used to handle moving entries in the library. The user can select multiple entries.
     * This will then trigger navigateForResult to DEST_NAME_PICKER with the
     */
    private val showSelectFolderButton = selectFolderMode && listMode == ListViewMode.PICKER

    private val moveContentEntriesUseCase: MoveContentEntriesUseCase by di.onActiveEndpoint().instance()

    private val deleteEntriesUseCase: DeleteContentEntryParentChildJoinUseCase by di.onActiveEndpoint().instance()

    private val exportContentEntryUseCase: ExportContentEntryUstadZipUseCase by di.onActiveEndpoint().instance()


    init {
        val savedStateSelectedEntries = savedStateHandle[KEY_SAVED_STATE_SELECTED_ENTRIES]?.let {
            json.decodeFromString(ListSerializer(ContentEntryListSelectedItem.serializer()), it)
        }?.toSet() ?: emptySet()

        _uiState.update { prev ->
            prev.copy(
                contentEntryList = pagingSourceFactory,
                showSelectFolderButton = showSelectFolderButton,
                selectedEntries = savedStateSelectedEntries,
                contextMenuItems = this::createContextMenuItemsForEntry,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MR.strings.content),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = {
                        _uiState.update { prev ->
                            prev.copy(
                                createNewOptionsVisible = true,
                            )
                        }
                    }
                ),
                hideBottomNavigation = listMode == ListViewMode.PICKER,
            )
        }

        //If the user is selecting ContentEntry for a CourseBlock and is not browsing
        //within library folders.
        if(hasCourseBlockArg && parentEntryUid == LIBRARY_ROOT_CONTENT_ENTRY_UID) {
            _uiState.update { prev ->
                prev.copy(
                    createNewFolderItemVisible = false,
                    importFromFileItemVisible = true,
                    importFromLinkItemVisible = true,
                    selectedChipId = savedStateHandle[KEY_FILTER_CHIP_ID]?.toInt() ?: FILTER_MY_CONTENT,
                    filterOptions = listOf(
                        MessageIdOption2(MR.strings.my_content, FILTER_MY_CONTENT),
                        MessageIdOption2(MR.strings.from_my_courses, FILTER_FROM_MY_COURSES),
                        MessageIdOption2(MR.strings.library, FILTER_FROM_LIBRARY)
                    )
                )
            }
        }

        val hasPermissionFlow = activeRepo.systemPermissionDao()
            .personHasSystemPermissionAsFlow(
                accountManager.currentAccount.personUid, PermissionFlags.EDIT_LIBRARY_CONTENT
            ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        viewModelScope.launch {
            defaultTitle = when {
                (expectedResultDest != null && !selectFolderMode) -> systemImpl.getString(MR.strings.select_content)
                parentEntryUid == LIBRARY_ROOT_CONTENT_ENTRY_UID -> systemImpl.getString(MR.strings.library)
                else -> activeRepo.contentEntryDao().findTitleByUidAsync(parentEntryUid) ?: ""
            }

            _appUiState.update { prev ->
                prev.copy(
                    title = defaultTitle,
                )
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                if(!hasCourseBlockArg) {
                    hasPermissionFlow.collect { hasNewContentPermission ->
                        _uiState.update { prev ->
                            if(prev.hasWritePermission != hasNewContentPermission) {
                                prev.copy(hasWritePermission = hasNewContentPermission)
                            }else {
                                prev
                            }
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(
                                    visible = hasNewContentPermission && !showSelectFolderButton
                                )
                            )
                        }
                    }
                }
            }
        }

        /*
         * Action moving items when the result is returned from picking the destination folder.
         */
        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(KEY_RESULT_MOVE_TO_DESTINATION_FOLDER).collect { result ->
                val destContentEntry = result.result as? ContentEntry ?: return@collect

                val selectedEntriesToMove = savedStateHandle[KEY_SAVED_STATE_ENTRIES_TO_MOVE]?.let {
                    json.decodeFromString(ListSerializer(ContentEntryListSelectedItem.serializer()), it)
                } ?: return@collect

                try {
                    moveContentEntriesUseCase(destContentEntry, selectedEntriesToMove.toSet())
                    snackDispatcher.showSnackBar(
                        Snack(
                            message = systemImpl.formatString(
                                MR.strings.moved_x_entries, selectedEntriesToMove.size.toString()
                            )
                        )
                    )
                }catch(e: Throwable) {
                    Napier.w("Could not move entries", throwable = e)
                    snackDispatcher.showSnackBar(
                        Snack(e.message ?: "")
                    )
                }

                setSelectedItems(emptySet())
            }
        }

        /*
         * Show move / delete icons when there is a selection AND the user has permission
         */
        viewModelScope.launch {
            _uiState.combine(hasPermissionFlow) { uiState, hasPermission ->
                Pair(uiState, hasPermission)
            }.collect {
                val showMoveIcon = it.first.selectedEntries.isNotEmpty() && it.second
                if(showMoveIcon != _appUiState.value.actionButtons.isNotEmpty()) {
                    _appUiState.update { prev ->
                        prev.copy(
                            actionButtons = if(showMoveIcon){
                                listOf(
                                    AppActionButton(
                                        icon = AppStateIcon.MOVE,
                                        contentDescription = systemImpl.getString(MR.strings.move),
                                        onClick = this@ContentEntryListViewModel::onClickMoveAction,
                                        id = "action_move"
                                    ),
                                    AppActionButton(
                                        icon = AppStateIcon.DELETE,
                                        contentDescription = systemImpl.getString(MR.strings.delete),
                                        onClick = this@ContentEntryListViewModel::onClickDeleteAction,
                                        id = "action_delete"
                                    )
                                )
                            }else {
                                emptyList()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Create right click context menu items. Function based on:
     *
     * https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Context_Menu/README.md
     */
    fun createContextMenuItemsForEntry(
        entry: ContentEntryAndListDetail
    ): List<UstadContextMenuItem> {
        val uiStateVal = _uiState.value
        return if(uiStateVal.hasWritePermission && listMode == ListViewMode.BROWSER) {
            //if the item that has been right clicked is not part of the current selection, then
            // clear the selection. Roughly the same behavior as file browsers.
            val rightClickedItem = entry.asSelectedItem()
            if(rightClickedItem !in uiStateVal.selectedEntries) {
                setSelectedItems(emptySet())
            }

            /**
             * If other items are selected, then add the item that was right clicked to
             * the selection and move/delete all items. If entry is already in the selection, use
             * selected entries without the need to update.
             *
             * If nothing was selected, leave the selection alone
             */
            fun entriesToAction(): Set<ContentEntryListSelectedItem> {
                val selectedEntries = _uiState.value.selectedEntries
                return when {
                    selectedEntries.isNotEmpty() && rightClickedItem !in selectedEntries -> {
                        (selectedEntries + rightClickedItem).toSet().also {
                            setSelectedItems(it)
                        }
                    }
                    selectedEntries.isEmpty() -> {  setOf(rightClickedItem) }
                    else -> selectedEntries
                }
            }

            listOf(
                UstadContextMenuItem(
                    label = systemImpl.getString(MR.strings.move_to),
                    onClick = {
                        selectDestinationToMoveEntries(entriesToAction())
                    },
                ),
                UstadContextMenuItem(
                    label = systemImpl.getString(MR.strings.delete),
                    onClick = {
                        launchDeleteEntries(entriesToAction())
                    }
                )
            )
        }else {
            emptyList()
        }
    }

    fun onExportContentEntry(contentEntryUid: Long) {
        viewModelScope.launch {
            try {
                val fileName = "export_$contentEntryUid.zip"
                val destZipFilePath = "${exportContentEntryUseCase.getOutputDirectory()}/$fileName"

                exportContentEntryUseCase(
                    contentEntryUid = contentEntryUid,
                    destZipFilePath = destZipFilePath,
                    progressListener = { progress ->
                        // Update UI with progress information
                        _uiState.update { it.copy(exportProgress = progress) }
                    }
                )

                // Handle successful export
                snackDispatcher.showSnackBar(
                    Snack("Successfully exported")
                )
            } catch (e: Exception) {
                // Handle export error
                Napier.e("Export failed", e)
                snackDispatcher.showSnackBar(
                    Snack("Exporting failed")
                )
            } finally {
                // Reset export progress
                _uiState.update { it.copy(exportProgress = null) }
            }
        }
    }
    override fun onUpdateSearchResult(searchText: String) {
        //do nothing
    }

    override fun onClickAdd() {
        //do nothing
    }

    fun onDismissCreateNewOptions() {
        _uiState.update { prev ->
            prev.copy(
                createNewOptionsVisible = false,
            )
        }
    }

    fun onClickNewFolder() {
        onDismissCreateNewOptions()
        navigateToCreateNew(
            editViewName = ContentEntryEditViewModel.DEST_NAME,
            extraArgs = buildMap {
                put(ContentEntryEditViewModel.ARG_LEAF, false.toString())
                put(ARG_PARENT_UID, parentEntryUid.toString())
            }
        )
    }

    fun onClickImportFromLink() {
        onDismissCreateNewOptions()
        navigateToCreateNew(
            editViewName = ContentEntryImportLinkViewModel.DEST_NAME,
            extraArgs = buildMap {
                put(ContentEntryEditViewModel.ARG_LEAF, true.toString())
                put(ARG_PARENT_UID, parentEntryUid.toString())
                put(ARG_NEXT, ContentEntryEditViewModel.DEST_NAME)
                putFromSavedStateIfPresent(CourseBlockEditViewModel.COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS)
            }
        )
    }

    fun onImportFile(fileUri: String, fileName: String) {
        onDismissCreateNewOptions()
        navigateToCreateNew(
            editViewName = ContentEntryGetMetadataViewModel.DEST_NAME,
            extraArgs = buildMap {
                put(ContentEntryGetMetadataViewModel.ARG_URI, fileUri)
                put(ContentEntryGetMetadataViewModel.ARG_FILENAME, fileName)
                put(ARG_PARENT_UID, parentEntryUid.toString())
                putFromSavedStateIfPresent(CourseBlockEditViewModel.COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS)
            }
        )
    }

    fun onClickEntry(entry: ContentEntry?) {
        if(entry == null)
            return

        val goToOnContentEntryEdit = savedStateHandle[ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE]?.toInt() ?: 0

        when {
            //If user is selecting a folder, and they have clicked on something that is not a folder, do nothing
            entry.leaf && showSelectFolderButton -> return

            //When the user is selecting a ContentEntry and then going onwards to CourseBlockEdit
            //As part of adding a content course block
            listMode == ListViewMode.PICKER && entry.leaf &&
                    goToOnContentEntryEdit == ContentEntryEditViewModel.GO_TO_COURSE_BLOCK_EDIT -> {

                navigateForResult(
                    nextViewName = CourseBlockEditViewModel.DEST_NAME,
                    key = ClazzEditViewModel.RESULT_KEY_COURSEBLOCK,
                    currentValue = null,
                    serializer = CourseBlockAndEditEntities.serializer(),
                    overwriteDestination = false,
                    args = buildMap {
                        putFromSavedStateIfPresent(CourseBlockEditViewModel.COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS)
                        put(
                            CourseBlockEditViewModel.ARG_SELECTED_CONTENT_ENTRY,
                            json.encodeToString(
                                ContentEntryAndContentJob.serializer(),
                                ContentEntryAndContentJob(entry)
                            )
                        )
                    }
                )
            }

            entry.leaf -> {
                navController.navigate(
                    viewName = ContentEntryDetailViewModel.DEST_NAME,
                    args = mapOf(
                        UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString(),
                        ARG_PARENT_UID to parentEntryUid.toString(),
                    )
                )
            }

            else -> {
                navController.navigate(
                    viewName = if(destinationName == DEST_NAME_HOME) {
                        DEST_NAME
                    }else {
                        destinationName
                    },
                    args = buildMap {
                        put(ARG_FILTER, FILTER_BY_PARENT_UID.toString())
                        put(ARG_PARENT_UID, entry.contentEntryUid.toString())
                        putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_KEY)
                        putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_VIEWNAME)
                        putFromSavedStateIfPresent(ARG_SELECT_FOLDER_MODE)
                        putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE)
                    }
                )
            }
        }
    }

    fun onClickSelectThisFolder() {
        finishWithResult(ContentEntry().apply {
            contentEntryUid = parentEntryUid
        })
    }

    fun onSetSelected(entry: ContentEntryAndListDetail, selected: Boolean) {
        //**MUST** commit to savedStateHandle

        val currentSelection = _uiState.value.selectedEntries
        setSelectedItems(
            if(selected) {
                (currentSelection + entry.asSelectedItem()).toSet()
            }else {
                currentSelection.filter {
                    it.contentEntryUid != (entry.contentEntry?.contentEntryUid ?: 0)
                }.toSet()
            }
        )
    }

    private fun setSelectedItems(selectedEntries: Set<ContentEntryListSelectedItem>) {
        savedStateHandle[KEY_SAVED_STATE_SELECTED_ENTRIES] = json.encodeToString(
            ListSerializer(ContentEntryListSelectedItem.serializer()), selectedEntries.toList()
        )

        _uiState.update { prev ->
            prev.copy(
                selectedEntries = selectedEntries
            )
        }

        val numItemsSelected = _uiState.value.selectedEntries.size
        val hasSelectedItems = numItemsSelected > 0

        /*
         * Showing icons in appUiState (e.g. move etc) is done by a flow collector (started in init)
         * that checks for permission AND selection.
         */
        _appUiState.update { prev ->
            prev.copy(
                userAccountIconVisible = !hasSelectedItems,
                hideSettingsIcon = hasSelectedItems,
                title = if(hasSelectedItems) {
                    systemImpl.formatPlural(MR.plurals.items_selected, numItemsSelected)
                }else {
                    defaultTitle
                },
                leadingActionButton = if(hasSelectedItems) {
                    AppActionButton(
                        icon = AppStateIcon.CLOSE,
                        contentDescription = systemImpl.getString(MR.strings.clear_selection),
                        onClick = {
                            setSelectedItems(emptySet())
                        },
                        id = "clear_selection"
                    )
                }else {
                    null
                },
                appBarColors = if(hasSelectedItems)
                    AppBarColors.SELECTION_MODE
                else
                    AppBarColors.STANDARD
            )
        }

    }

    /**
     * Initiate navigation to select a destination for moving entries. This can be a result of a
     * context menu click on a specific item, or when the user has made a selection and then clicks
     * the button on the action bar
     */
    private fun selectDestinationToMoveEntries(entries: Set<ContentEntryListSelectedItem>) {
        savedStateHandle[KEY_SAVED_STATE_ENTRIES_TO_MOVE] = json.encodeToString(
            ListSerializer(ContentEntryListSelectedItem.serializer()), entries.toList()
        )

        navigateForResult(
            nextViewName = DEST_NAME_PICKER,
            key = KEY_RESULT_MOVE_TO_DESTINATION_FOLDER,
            currentValue = null,
            serializer = ContentEntry.serializer(),
            args = mapOf(
                ARG_LISTMODE to ListViewMode.PICKER.mode,
                ARG_SELECT_FOLDER_MODE to true.toString(),
            )
        )
    }

    private fun onClickMoveAction() {
        selectDestinationToMoveEntries(_uiState.value.selectedEntries)
    }

    private fun launchDeleteEntries(entries: Set<ContentEntryListSelectedItem>) {
        viewModelScope.launch {
            deleteEntriesUseCase(
                entries = entries,
                activeUserPersonUid = activeUserPersonUid
            )

            setSelectedItems(emptySet())

            snackDispatcher.showSnackBar(
                Snack(systemImpl.formatPlural(MR.plurals.items_deleted, entries.size))
            )
        }
    }

    private fun onClickDeleteAction() {
        launchDeleteEntries(_uiState.value.selectedEntries)
    }


    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.takeIf { it.value.selectedChipId != filterOption.value }?.update { prev ->
            prev.copy(
                selectedChipId = filterOption.value
            )
        }
        savedStateHandle[KEY_FILTER_CHIP_ID] = filterOption.value.toString()
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    companion object {


        const val DEST_NAME = "ContentEntries"

        const val DEST_NAME_HOME = "ContentEntryListHome"

        /**
         * Note: Because picker mode may involve more than one step in the back stack, we need a
         * different destination/view name so that popupTo will work
         */
        const val DEST_NAME_PICKER = "PickContentEntry"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME, DEST_NAME_PICKER)

        const val ARG_FILTER = "filter"

        const val FILTER_BY_PARENT_UID = 1

        const val FILTER_MY_CONTENT = 2

        const val FILTER_FROM_MY_COURSES = 3

        const val FILTER_FROM_LIBRARY = 4

        const val LIBRARY_ROOT_CONTENT_ENTRY_UID = 1L

        private const val KEY_FILTER_CHIP_ID = "chipId"

        const val KEY_RESULT_MOVE_TO_DESTINATION_FOLDER = "moveToDestinationResult"

        const val ARG_SELECT_FOLDER_MODE = "selectFolder"

        const val KEY_SAVED_STATE_SELECTED_ENTRIES = "selectedEntries"

        const val KEY_SAVED_STATE_ENTRIES_TO_MOVE = "entriesToMove"


    }
}