package com.ustadmobile.core.viewmodel.scopedgrant.list

import com.ustadmobile.core.db.dao.PersonDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.ScopedGrantListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.composites.ScopedGrantEntityAndName
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ScopedGrantListUiState(

    val scopeGrantList: () -> PagingSource<Int, ScopedGrantEntityAndName> = { EmptyPagingSource() },

    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MessageID.name, PersonDaoCommon.SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MessageID.name, PersonDaoCommon.SORT_FIRST_NAME_DESC, false),
    ),

    val sortOption: SortOrderOption = sortOptions.first(),

    val showAddItem: Boolean = false,
)

class ScopedGrantListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = ScopedGrantListView.VIEW_NAME,
): UstadListViewModel<ScopedGrantListUiState>(
    di, savedStateHandle, ScopedGrantListUiState(), destinationName
) {

    private val argTableId = savedStateHandle[UstadView.ARG_CODE_TABLE]?.toInt() ?: 0

    private val argEntityId = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private var lastPagingSource: PagingSource<Int, ScopedGrantEntityAndName>? = null

    private val pagingSourceFactory: () -> PagingSource<Int, ScopedGrantEntityAndName> = {
        activeRepo.scopedGrantDao.findByTableIdAndEntityUidWithNameAsPagingSource(
            argTableId, argEntityId, _uiState.value.sortOption.flag,
            _appUiState.value.searchState.searchText.toQueryLikeParam()
        ).also {
            lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MessageID.permissions, MessageID.permission),
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MessageID.permission),
                    icon = FabUiState.FabIcon.ADD,
                )

            )
        }

        _uiState.update{ prev ->
            prev.copy(
                scopeGrantList = pagingSourceFactory
            )
        }

        viewModelScope.launch {

            _uiState.whenSubscribed {
                activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                    accountManager.activeAccount.personUid, Role.PERMISSION_CLAZZ_UPDATE
                ).collect{ hasAddPermission ->
                    _appUiState.update {prev ->
                        prev.copy(fabState = prev.fabState.copy(visible = hasAddPermission))
                    }

                }
            }

            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.activeAccount.personUid, Role.PERMISSION_CLAZZ_UPDATE)

                },
                fabMessageId = MessageID.permission,

                onSetAddListItemVisibility = { visible ->
                    _uiState.update { prev -> prev.copy(showAddItem = visible ) }
                }
            )

            launch{
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_PERSON_SELECT).collect{
                    val person = it.result as? Person ?: return@collect

                    val args = mapOf(
                        UstadView.ARG_CODE_TABLE to argTableId.toString(),
                        UstadView.ARG_ENTITY_UID to argEntityId.toString(),
                        UstadView.ARG_PERSON_UID to person.personUid.toString()
                    )

                    navigateToCreateNew(ScopedGrantEditView.VIEW_NAME, args)

                }
            }
        }


    }


    override fun onUpdateSearchResult(searchText: String) {
        lastPagingSource?.invalidate()
    }

    fun onSortOrderChanged(sortOption: SortOrderOption){
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption
            )
        }

    }

    override fun onClickAdd() {
        //Navigate to Select Person > ScopedGrantEdit

        val goToOnPersonSelectedArgs = ScopedGrantEditView.VIEW_NAME
            .appendQueryArgs(mapOf(
                UstadView.ARG_CODE_TABLE to argTableId.toString(),
                UstadView.ARG_ENTITY_UID to argEntityId.toString(),
            ))

        val args = mutableMapOf(
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.mode,
            PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED to goToOnPersonSelectedArgs,
        )

        navController.navigate(
            viewName = PersonListViewModel.DEST_NAME,
            args = args
        )

    }

    fun onClickEntry(entry: ScopedGrantEntityAndName){
        val scopedGrant = entry.scopedGrant?:return
        navigateOnItemClicked(ScopedGrantDetailView.VIEW_NAME, scopedGrant.sgUid, scopedGrant)
    }

    companion object{
        const val RESULT_KEY_PERSON_SELECT = "ScopedGrantPersonSelect"
    }
}