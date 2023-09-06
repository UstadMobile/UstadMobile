package com.ustadmobile.core.viewmodel.contententry.list

import com.ustadmobile.core.db.dao.ContentEntryDaoCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel.Companion.FILTER_BY_PARENT_UID
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer as ContentEntryAndExtras
import org.kodein.di.DI

data class ContentEntryListUiState(

    val filterMode: Int = FILTER_BY_PARENT_UID,

    val contentEntryList: ListPagingSourceFactory<ContentEntryAndExtras> = {
        EmptyPagingSource()
    },

    val selectedChipId: Int = FILTER_BY_PARENT_UID,

    val showChips: Boolean = false,

    val showHiddenEntries: Boolean = false,

    val onlyFolderFilter: Boolean = false,

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OPTIONS,

    val activeSortOption: SortOrderOption = sortOptions.first(),

    val addNewItemVisible: Boolean = false,

) {
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

    private val pagingSourceFactory: ListPagingSourceFactory<ContentEntryAndExtras> = {
        when(_uiState.value.selectedChipId) {
            FILTER_MY_CONTENT -> activeRepo.contentEntryDao.getContentByOwner(activeUserPersonUid)

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
            lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, ContentEntryAndExtras>? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                contentEntryList = pagingSourceFactory
            )
        }
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.library),
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MR.strings.content),
                    icon = FabUiState.FabIcon.ADD,
                )
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                    accountManager.activeAccount.personUid, Role.PERMISSION_CONTENT_INSERT
                ).collect { hasNewContentPermission ->
                    _appUiState.update { prev ->
                        prev.copy(fabState = prev.fabState.copy(visible = hasNewContentPermission))
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

    fun onClickNewFolder() {
        navigateToCreateNew(
            editViewName = ContentEntryEditViewModel.DEST_NAME,
            extraArgs = buildMap {
                put(ContentEntryEditViewModel.ARG_LEAF, false.toString())
                put(ARG_PARENT_UID, parentEntryUid.toString())
            }
        )
    }

    fun onClickEntry(entry: ContentEntryAndExtras?) {
        if(entry == null)
            return

        if(entry.leaf) {
            navController.navigate(
                viewName = ContentEntryDetailView.VIEW_NAME,
                args = mapOf(UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString())
            )
        }else {
            navController.navigate(
                viewName = DEST_NAME,
                args = mapOf(
                    ARG_FILTER to FILTER_BY_PARENT_UID.toString(),
                    ARG_PARENT_UID to entry.contentEntryUid.toString(),
                )
            )
        }
    }

    companion object {

        const val DEST_NAME = "ContentEntries"

        /**
         * Note: Because picker mode may involve more than one step in the back stack, we need a
         * different destination/view name so that popupTo will work
         */
        const val DEST_NAME_PICKER = "PickContentEntry"

        const val ARG_FILTER = "filter"

        const val ARG_SHOW_FILTER_CHIPS = "showChips"

        const val FILTER_BY_PARENT_UID = 1

        const val FILTER_MY_CONTENT = 2

        const val FILTER_FROM_MY_COURSES = 3

        const val FILTER_FROM_LIBRARY = 4

        const val LIBRARY_ROOT_CONTENT_ENTRY_UID = 1L


    }
}