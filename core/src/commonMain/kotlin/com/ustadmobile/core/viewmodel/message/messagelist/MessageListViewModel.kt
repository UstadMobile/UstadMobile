package com.ustadmobile.core.viewmodel.message.messagelist

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.dayStringResource
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Message
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI

data class MessageListUiState(
    val messages: () -> PagingSource<Int, Message> = { EmptyPagingSource() },
    val activePersonUid: Long = 0,
    val newMessageText: String = "",
    val dayOfWeekStrings: Map<DayOfWeek, String> = emptyMap(),
    val localDateTimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

class MessageListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<MessageListUiState>(
    di, savedStateHandle, MessageListUiState(), destinationName
) {

    private val otherPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0L

    private val pagingSourceFactory: () -> PagingSource<Int, Message> = {
        activeRepo.messageDao().messagesFromOtherUserAsPagingSource(
            accountPersonUid = activeUserPersonUid,
            otherPersonUid = otherPersonUid
        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                userAccountIconVisible = false,
                hideBottomNavigation = true,
            )
        }

        _uiState.update { prev ->
            prev.copy(
                dayOfWeekStrings = DayOfWeek.values().associateWith {
                    systemImpl.getString(it.dayStringResource)
                },
                messages = pagingSourceFactory,
                activePersonUid = activeUserPersonUid
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.personDao().getNamesByUid(otherPersonUid).collect {
                    _appUiState.update { prev ->
                        prev.copy(
                            title = "${it?.firstNames ?: ""} ${it?.lastName ?: ""}"
                        )
                    }
                }
            }

        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        //do nothing - there is no add here
    }

    fun onChangeNewMessageText(
        text: String
    ) {
        _uiState.update { prev ->
            prev.copy(newMessageText = text)
        }
    }

    fun onClickSend() {
        viewModelScope.launch {
            activeRepo.messageDao().insert(
                Message(
                    messageSenderPersonUid = activeUserPersonUid,
                    messageText = _uiState.value.newMessageText.trim(),
                    messageToPersonUid = otherPersonUid,
                    messageTimestamp = systemTimeInMillis(),
                )
            )

            _uiState.update { prev ->
                prev.copy(
                    newMessageText = ""
                )
            }
        }
    }


    companion object {

        const val DEST_NAME = "MessageList"

    }

}

