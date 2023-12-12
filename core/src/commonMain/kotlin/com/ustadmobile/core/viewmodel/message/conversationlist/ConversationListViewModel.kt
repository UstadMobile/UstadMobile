package com.ustadmobile.core.viewmodel.message.conversationlist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ConversationListUiState(
    val conversations: () -> PagingSource<Int, MessageAndSenderPerson> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = emptyList(), //Should be by name (ascending/descending), time (ascending/descending)
    val showAddItem: Boolean = false,
)

class ConversationListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<ConversationListUiState>(
    di, savedStateHandle, ConversationListUiState(), destinationName
) {

    private val pagingSourceFactory: () -> PagingSource<Int, ChatWithLatestMessageAndCount> = {
        activeRepo.chatDao.findAllChatsForUser(
            "",
            accountManager.currentAccount.personUid,
        ).also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, ChatWithLatestMessageAndCount>? = null

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

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_ADD_PERSON).collect { result ->
                    val person = result.result as? Person ?: return@collect
                    onClickStartChat(person)
                }
            }

            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.currentAccount.personUid, Role.PERMISSION_PERSON_INSERT
                    )
                },
                fabStringResource = MR.strings.message,
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
        navigateForResult(
            nextViewName = PersonListViewModel.DEST_NAME,
            key = RESULT_KEY_ADD_PERSON,
            currentValue = Person(),
            serializer = Person.serializer()
        )
    }

    private fun onClickStartChat(entry: Person) {
        navController.navigate(ConversationListViewModel.DEST_NAME,
            mapOf(UstadView.ARG_PERSON_UID to 0.toString()))
    }


    companion object {

        const val DEST_NAME = "ConversationList"

        const val DEST_NAME_HOME = "ConversationListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

        const val RESULT_KEY_ADD_PERSON = "Schedule"

    }

}


