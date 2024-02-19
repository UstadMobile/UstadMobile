package com.ustadmobile.core.viewmodel.coursegroupset.list

import com.ustadmobile.core.db.dao.CourseGroupSetDaoConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.collectClazzNameAndUpdateTitle
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Role
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class CourseGroupSetListUiState(

    val showAddItem: Boolean = false,

    val courseGroupSets: ListPagingSourceFactory<CourseGroupSet> = { EmptyPagingSource() },

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OPTIONS,

    val sortOption: SortOrderOption = sortOptions.first(),

) {
    companion object {

        val DEFAULT_SORT_OPTIONS = listOf(
            SortOrderOption(MR.strings.name_key, CourseGroupSetDaoConstants.SORT_NAME_ASC, true),
            SortOrderOption(MR.strings.name_key, CourseGroupSetDaoConstants.SORT_NAME_DESC, false),
        )

    }
}

class CourseGroupSetListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<CourseGroupSetListUiState>(
    di, savedStateHandle, CourseGroupSetListUiState(), DEST_NAME
) {

    private val clazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

    private val pagingSourceFactory: ListPagingSourceFactory<CourseGroupSet> = {
        activeRepo.courseGroupSetDao.findAllCourseGroupSetForClazz(
            clazzUid = clazzUid,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            sortOrder = _uiState.value.sortOption.flag,
        ).also {
            mLastPagingSource = it
        }
    }

    private var mLastPagingSource: PagingSource<Int, CourseGroupSet>? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                courseGroupSets = pagingSourceFactory,
            )
        }

        _appUiState.update {prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
            )
        }

        viewModelScope.launch {
            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    activeRepo.clazzDao.personHasPermissionWithClazzAsFlow(
                        activeUserPersonUid, clazzUid, Role.PERMISSION_CLAZZ_UPDATE
                    )
                },
                fabStringResource = MR.strings.groups,
                onSetAddListItemVisibility = { visible ->
                    _uiState.update { prev ->
                        Napier.v { "CourseGroupSetList: set showAddItem visible = $visible" }
                        prev.copy(showAddItem = visible)
                    }
                }
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    collectClazzNameAndUpdateTitle(clazzUid, activeDb, _appUiState)
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        mLastPagingSource?.invalidate()
    }

    fun onSortOptionChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(sortOption = sortOption)
        }
        mLastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        navigateToCreateNew(
            editViewName = CourseGroupSetEditViewModel.DEST_NAME,
            extraArgs = mapOf(UstadView.ARG_CLAZZUID to clazzUid.toString())
        )
    }

    fun onClickEntry(entry: CourseGroupSet){
        navigateOnItemClicked(
            detailViewName = CourseGroupSetDetailViewModel.DEST_NAME,
            entityUid = entry.cgsUid,
            result = entry
        )
    }

    companion object {

        const val DEST_NAME = "CourseGroups"

    }
}