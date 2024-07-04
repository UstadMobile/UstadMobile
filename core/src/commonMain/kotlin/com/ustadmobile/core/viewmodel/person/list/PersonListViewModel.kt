package com.ustadmobile.core.viewmodel.person.list

import com.ustadmobile.core.db.dao.PersonDaoCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactViewModel
import com.ustadmobile.core.viewmodel.clazz.invitevialink.InviteViaLinkViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants.ARG_POPUP_TO_ON_PERSON_SELECTED
import com.ustadmobile.core.viewmodel.person.bulkaddselectfile.BulkAddPersonSelectFileViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.lib.db.composites.PersonAndListDisplayDetails
import org.kodein.di.instance

data class PersonListUiState(
    val personList: () -> PagingSource<Int, PersonAndListDisplayDetails> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.first_name, PersonDaoCommon.SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MR.strings.first_name, PersonDaoCommon.SORT_FIRST_NAME_DESC, false),
        SortOrderOption(MR.strings.last_name, PersonDaoCommon.SORT_LAST_NAME_ASC, true),
        SortOrderOption(MR.strings.last_name, PersonDaoCommon.SORT_LAST_NAME_DESC, false)
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showAddItem: Boolean = false,
    val showInviteViaLink: Boolean = false,
    val showInviteViaContact: Boolean = false,
    val inviteCode: String? = null,
    val showSortOptions: Boolean = true,
    val addSheetOrDialogVisible: Boolean = false,
    val hasBulkImportPermission: Boolean = false,
)

class EmptyPagingSource<Key: Any, Value: Any>: PagingSource<Key, Value>() {

    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return null
    }



    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun load(params: PagingSourceLoadParams<Key>): PagingSourceLoadResult<Key, Value> {
        return PagingSourceLoadResultPage<Key, Value>(emptyList(), null, null)
            as PagingSourceLoadResult<Key, Value>
    }
}




class PersonListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<PersonListUiState>(
    di, savedStateHandle, PersonListUiState(), destinationName
) {

    private val filterExcludeMembersOfClazz = savedStateHandle[ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ]?.toLong() ?: 0L
    private val personRole = savedStateHandle[ARG_ROLE]?.toLong() ?: 0L

    private val filterAlreadySelectedList = savedStateHandle[ARG_EXCLUDE_PERSONUIDS_LIST]
        ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
        ?: listOf()

    private val permissionRequiredToShowList =
        savedStateHandle[ARG_REQUIRE_PERMISSION_TO_SHOW_LIST]?.toLong() ?: 0

    private val pagingSourceFactory: () -> PagingSource<Int, PersonAndListDisplayDetails> = {
        activeRepo.personDao.findPersonsWithPermissionAsPagingSource(
            timestamp = getSystemTimeInMillis(),
            excludeClazz = filterExcludeMembersOfClazz,
            excludeSelected = filterAlreadySelectedList,
            accountPersonUid = activeUserPersonUid,
            sortOrder = _uiState.value.sortOption.flag,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam()
        )
    }

    private val inviteCode = savedStateHandle[ARG_SHOW_ADD_VIA_INVITE_LINK_CODE]

    private val showInviteViaContact = savedStateHandle[ARG_SHOW_ADD_VIA_CONTACT]=="true"

    private val setClipboardStringUseCase: SetClipboardStringUseCase by instance()


    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(visible = false),
                title = savedStateHandle[ARG_TITLE] ?: listTitle(MR.strings.people, MR.strings.select_person),
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.person),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = this::onClickFab,
                )
            )
        }

        val hasPermissionToListFlow = if(permissionRequiredToShowList == 0L) {
            flowOf(true)
        }else {
            activeRepo.systemPermissionDao.personHasSystemPermissionAsFlow(
                accountPersonUid = activeUserPersonUid,
                permission = permissionRequiredToShowList,
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                hasPermissionToListFlow.distinctUntilChanged().collect { hasPermissionToList ->
                    _uiState.update { prev ->
                        prev.copy(
                            personList = if(hasPermissionToList) {
                                pagingSourceFactory
                            }else {
                                { EmptyPagingSource() }
                            },
                            showInviteViaLink = inviteCode != null,
                            inviteCode = inviteCode,
                            showInviteViaContact = showInviteViaContact,
                            showSortOptions = hasPermissionToList,
                        )
                    }
                    _appUiState.update { prev ->
                        prev.copy(
                            searchState = prev.searchState.copy(
                                visible = hasPermissionToList,
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            activeRepo.systemPermissionDao.personHasSystemPermissionPairAsFlow(
                accountPersonUid = activeUserPersonUid,
                firstPermission = PermissionFlags.ADD_PERSON,
                secondPermission = PermissionFlags.PERSON_VIEW
            ).collect {
                val (hasAddPermission, hasViewAllPermission) = it
                val hasBulkAddPermission = hasAddPermission && hasViewAllPermission
                _uiState.update { prev ->
                    prev.copy(
                        showAddItem = listMode == ListViewMode.PICKER && hasAddPermission,
                        hasBulkImportPermission = hasBulkAddPermission,
                    )
                }
                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = listMode == ListViewMode.BROWSER && hasAddPermission
                        )
                    )
                }
            }
        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        //will use the searchText as per the appUiState
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption
            )
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onClickInviteWithLink() {
        if(inviteCode == null)
            return //could never happen - button would not show if this was null

        navController.navigate(
            InviteViaLinkViewModel.DEST_NAME,
            args = mapOf(
                ARG_INVITE_CODE to inviteCode
            )
        )
    }

    fun onClickCopyInviteCode() {
        _uiState.value.inviteCode?.also { inviteCode ->
            setClipboardStringUseCase(inviteCode)
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.copied_to_clipboard)))
        }
    }
    fun onClickInviteViaContact() {
        val args = buildMap {
            put(InviteViaContactViewModel.ARG_ROLE, personRole.toString())
            put(InviteViaContactViewModel.ARG_CLAZZ_UID, filterExcludeMembersOfClazz.toString())
        }

        navController.navigate(
            viewName = InviteViaContactViewModel.DEST_NAME,
            args = args
        )


    }

    private fun onClickFab() {
        if(_uiState.value.hasBulkImportPermission) {
            _uiState.update { prev -> prev.copy(addSheetOrDialogVisible = true) }
        }else {
            onClickAdd()
        }
    }

    fun onClickBulkAdd() {
        navController.navigate(BulkAddPersonSelectFileViewModel.DEST_NAME, emptyMap())
    }

    override fun onClickAdd() {
        navigateToCreateNew(PersonEditViewModel.DEST_NAME, savedStateHandle[ARG_GO_TO_ON_PERSON_SELECTED]?.let {
            mapOf(ARG_GO_TO_ON_PERSON_SELECTED to it)
        } ?: emptyMap())
    }

    fun onClickEntry(entry: Person) {
        val goToOnPersonSelected = savedStateHandle[ARG_GO_TO_ON_PERSON_SELECTED]

        if(goToOnPersonSelected != null) {
            val args = UMFileUtil.parseURLQueryString(goToOnPersonSelected) +
                mapOf(UstadView.ARG_PERSON_UID to entry.personUid.toString())
            val goToDestName = goToOnPersonSelected.substringBefore("?")
            val popUpTo = savedStateHandle[ARG_POPUP_TO_ON_PERSON_SELECTED]
            navController.navigate(
                goToDestName, args, UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = popUpTo,
                    popUpToInclusive = true,
                )
            )
        }else {
            navigateOnItemClicked(PersonDetailViewModel.DEST_NAME, entry.personUid, entry)
        }
    }

    fun onDismissAddSheetOrDialog() {
        _uiState.update { it.copy(addSheetOrDialogVisible = false) }
    }

    companion object {

        const val DEST_NAME = "People"

        const val DEST_NAME_HOME = "PersonListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

        /**
         * Exclude those who are already in the given class. This is useful for
         * the add to class picker (e.g. to avoid showing people who are already in the
         * given class)
         */
        const val ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ = "exlcudeFromClazz"

        const val ARG_EXCLUDE_PERSONUIDS_LIST = "excludeAlreadySelectedList"

        const val ARG_SHOW_ADD_VIA_INVITE_LINK_CODE = "showAddViaInviteLink"

        /**
         * to give this option only while adding users to course
         */
        const val ARG_SHOW_ADD_VIA_CONTACT= "showAddViaContact"
        const val ARG_ROLE= "role"

        /**
         * Require a specific system permission to show the list. This has no security implication,
         * because it will be enforced on navigating to the next screen.
         *
         * When this is used as part of the enrol student/teacher flow, this avoids showing a list
         * of people when the user does not have permission to actually enrol them.
         */
        const val ARG_REQUIRE_PERMISSION_TO_SHOW_LIST = "rptsl"


    }

}


