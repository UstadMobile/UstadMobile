package com.ustadmobile.core.viewmodel.message.conversationlist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class ConversationListUiState(
    val conversations: () -> PagingSource<Int, MessageAndOtherPerson> = { EmptyPagingSource() },
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

    private val pagingSourceFactory: () -> PagingSource<Int, MessageAndOtherPerson> = {
        activeRepo.messageDao.conversationsForUserAsPagingSource(
            searchQuery =  _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = activeUserPersonUid,
        ).also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, MessageAndOtherPerson>? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                conversations = pagingSourceFactory
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.messages, MR.strings.select_person),
                fabState = FabUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.message),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = this@ConversationListViewModel::onClickAdd
                )
            )
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        //will use the searchText as per the appUiState
        lastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        navController.navigate(
            viewName = PersonListViewModel.DEST_NAME,
            args = mapOf(
                PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED to MessageListViewModel.DEST_NAME,
                UstadView.ARG_LISTMODE to ListViewMode.PICKER.mode,
                PersonListViewModel.ARG_EXCLUDE_PERSONUIDS_LIST to activeUserPersonUid.toString(),
                PersonViewModelConstants.ARG_POPUP_TO_ON_PERSON_SELECTED to PersonListViewModel.DEST_NAME,
            )
        )
    }

    fun onClickEntry(entry: MessageAndOtherPerson) {
        navController.navigate(
            MessageListViewModel.DEST_NAME,
            mapOf(UstadView.ARG_PERSON_UID to (entry.otherPerson?.personUid ?: 0).toString())
        )
    }


    companion object {

        const val DEST_NAME = "ConversationList"

        const val DEST_NAME_HOME = "ConversationListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

    }

}


