package com.ustadmobile.core.viewmodel.message.messagelist

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson
import com.ustadmobile.lib.db.entities.MessageWithPerson
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class MessageListUiState(
    val messages: () -> PagingSource<Int, MessageAndSenderPerson> = { EmptyPagingSource() },
    val activePersonUid: Long = 0,
)

class MessageListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<MessageListUiState>(
    di, savedStateHandle, MessageListUiState(), destinationName
) {

//    private val pagingSourceFactory: () -> PagingSource<Int, MessageAndSenderPerson> = {
//        activeRepo.messageDao.findAllMessagesByChatUid(
//            getSystemTimeInMillis(), 1,
//            accountManager.currentAccount.personUid
//        ).also {
//            lastPagingSource = it
//        }
//    }

    private var lastPagingSource: PagingSource<Int, MessageWithPerson>? = null

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.message, MR.strings.message)
            )
        }

//        _uiState.update { prev ->
//            prev.copy(
//                conversations = pagingSourceFactory
//            )
//        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    companion object {

        const val DEST_NAME = "MessageList"

    }

}

