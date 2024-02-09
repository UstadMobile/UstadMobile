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
import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.appstate.AppActionButton
import com.ustadmobile.core.impl.appstate.AppBarColors
import com.ustadmobile.core.impl.appstate.AppStateIcon
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkViewModel
import com.ustadmobile.lib.db.composites.ContentEntryBlockLanguageAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ContentEntryListUiState(

    val filterMode: Int = FILTER_BY_PARENT_UID,

    val contentEntryList: ListPagingSourceFactory<ContentEntry> = { EmptyPagingSource() },

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

    val selectedUids: Set<Long> = emptySet(),

) {

    val showChips: Boolean
        get() =filterOptions.isNotEmpty()

    companion object {

        val DEFAULT_SORT_OPTIONS = listOf(
            SortOrderOption(MR.strings.title, ContentEntryDaoCommon.SORT_TITLE_ASC, true),
            SortOrderOption(MR.strings.title, ContentEntryDaoCommon.SORT_TITLE_DESC, false),
        )

    }
}


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
    private val hasCourseBlockArg: Boolean = savedStateHandle[ContentEntryEditViewModel.ARG_COURSEBLOCK] != null

    private val pagingSourceFactory: ListPagingSourceFactory<ContentEntry> = {
        when(_uiState.value.selectedChipId) {
            FILTER_MY_CONTENT -> activeRepo.contentEntryDao.getContentByOwner(
                activeUserPersonUid
            )

            FILTER_FROM_MY_COURSES -> activeRepo.contentEntryDao.getContentFromMyCourses(
                activeUserPersonUid
            )

            FILTER_FROM_LIBRARY -> {
                activeRepo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                    parentEntryUid, 0, 0, activeUserPersonUid, false,
                    false,
                    _uiState.value.activeSortOption.flag
                )
            }

            FILTER_BY_PARENT_UID -> {
                activeRepo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByName(
                    parentEntryUid, 0, 0, activeUserPersonUid,
                    _uiState.value.showHiddenEntries, _uiState.value.onlyFolderFilter,
                    _uiState.value.activeSortOption.flag
                )
            }

            else -> EmptyPagingSource()
        }.also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, ContentEntry>? = null

    private var defaultTitle: String = ""

    init {
        _uiState.update { prev ->
            prev.copy(
                contentEntryList = pagingSourceFactory
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

        viewModelScope.launch {
            defaultTitle = when {
                expectedResultDest != null -> systemImpl.getString(MR.strings.select_content)
                parentEntryUid == LIBRARY_ROOT_CONTENT_ENTRY_UID -> systemImpl.getString(MR.strings.library)
                else -> activeRepo.contentEntryDao.findTitleByUidAsync(parentEntryUid) ?: ""
            }

            _appUiState.update { prev ->
                prev.copy(
                    title = defaultTitle
                )
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                if(!hasCourseBlockArg) {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.currentAccount.personUid, Role.PERMISSION_CONTENT_INSERT
                    ).collect { hasNewContentPermission ->
                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(
                                    visible = hasNewContentPermission
                                )
                            )
                        }
                    }
                }
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
                putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_COURSEBLOCK)
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
                putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_COURSEBLOCK)
            }
        )
    }

    fun onClickEntry(entry: ContentEntry?) {
        if(entry == null)
            return

        //When the user is selecting content from ClazzEdit
        val courseBlockArg = savedStateHandle[ContentEntryEditViewModel.ARG_COURSEBLOCK]
        if(entry.leaf && courseBlockArg != null) {
            val courseBlock = json.decodeFromString(
                deserializer = CourseBlock.serializer(),
                string = courseBlockArg,
            ).shallowCopy {
                cbTitle = entry.title
                cbDescription = entry.description
            }

            navigateForResult(
                nextViewName = ContentEntryEditViewModel.DEST_NAME,
                key = ClazzEditViewModel.RESULT_KEY_CONTENTENTRY,
                currentValue = ContentEntryBlockLanguageAndContentJob(
                    entry = entry,
                    block = courseBlock,
                    contentJob = null,
                    contentJobItem = null,
                ),
                serializer = ContentEntryBlockLanguageAndContentJob.serializer(),
                overwriteDestination = false
            )
            return
        }


        if(entry.leaf) {
            navController.navigate(
                viewName = ContentEntryDetailViewModel.DEST_NAME,
                args = mapOf(UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString())
            )
        }else {
            navController.navigate(
                viewName = DEST_NAME,
                args = buildMap {
                    put(ARG_FILTER, FILTER_BY_PARENT_UID.toString())
                    put(ARG_PARENT_UID, entry.contentEntryUid.toString())
                    putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_COURSEBLOCK)
                    putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_KEY)
                    putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_VIEWNAME)
                }
            )
        }
    }

    fun onSetSelected(uid: Long, selected: Boolean) {
        val currentSelection = _uiState.value.selectedUids
        setSelectedItems(
            if(selected) {
                (currentSelection + uid).toSet()
            }else {
                currentSelection.filter { it != uid }.toSet()
            }
        )
    }

    private fun setSelectedItems(selectedUids: Set<Long>) {
        _uiState.update { prev ->
            prev.copy(
                selectedUids = selectedUids
            )
        }

        val numItemsSelected = _uiState.value.selectedUids.size
        val hasSelectedItems = numItemsSelected > 0

        _appUiState.update { prev ->
            prev.copy(
                actionButtons = if(hasSelectedItems){
                    listOf(
                        AppActionButton(
                            icon = AppStateIcon.MOVE,
                            contentDescription = systemImpl.getString(MR.strings.move),
                            onClick = this@ContentEntryListViewModel::onClickMoveAction
                        )
                    )
                }else {
                    emptyList()
                },
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
                        }
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

    private fun onClickMoveAction() {

    }


    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.takeIf { it.value.selectedChipId != filterOption.value }?.update { prev ->
            prev.copy(
                selectedChipId = filterOption.value
            )
        }
        savedStateHandle[KEY_FILTER_CHIP_ID] = filterOption.value.toString()
        lastPagingSource?.invalidate()
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


    }
}