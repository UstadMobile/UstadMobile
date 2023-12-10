package com.ustadmobile.core.viewmodel.message.messagelist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class MessageListUiState(
    val messages: () -> PagingSource<Int, MessageAndSenderPerson> = { EmptyPagingSource() },
    val activePersonUid: Long = 0,
    val sortOptions: List<SortOrderOption> = emptyList(), //Should be by name (ascending/descending), time (ascending/descending)
    val showAddItem: Boolean = false,
)

class MessageListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<MessageListUiState>(
    di, savedStateHandle, MessageListUiState(), destinationName
) {

    private val pagingSourceFactory: () -> PagingSource<Int, MessageWithPerson> = {
        activeRepo.messageDao.findAllMessagesByChatUid(
            getSystemTimeInMillis(),
            1,
            accountManager.currentAccount.personUid,
        ).also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, MessageWithPerson>? = null

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.messages, MR.strings.select_person)
            )
        }

//        _uiState.update { prev ->
//            prev.copy(
//                messages = pagingSourceFactory
//            )
//        }

        viewModelScope.launch {
            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.currentAccount.personUid, Role.PERMISSION_PERSON_INSERT
                    )
                },
                fabStringResource = MR.strings.messages,
                onSetAddListItemVisibility = { visible ->
                    _uiState.update { prev -> prev.copy(showAddItem = visible) }
                }
            )
        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        navigateToCreateNew(PersonEditViewModel.DEST_NAME, savedStateHandle[PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED]?.let {
            mapOf(PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED to it)
        } ?: emptyMap())
    }

    fun onClickEntry(entry: Person) {
        val goToOnPersonSelected = savedStateHandle[PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED]

        if(goToOnPersonSelected != null) {
            val args = UMFileUtil.parseURLQueryString(goToOnPersonSelected) +
                    mapOf(UstadView.ARG_PERSON_UID to entry.personUid.toString())
            val goToDestName = goToOnPersonSelected.substringBefore("?")
            navController.navigate(goToDestName, args)
        }else {
            navigateOnItemClicked(PersonDetailViewModel.DEST_NAME, entry.personUid, entry)
        }
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.messages, MR.strings.select_person)
            )
        }


    }


    companion object {

        const val DEST_NAME = "Message"

        const val DEST_NAME_HOME = "MessageListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

    }

}

