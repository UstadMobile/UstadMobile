package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class DiscussionPostDetailUiState(
    val discussionPost: DiscussionPostWithDetails? = null,
    val replies: List<DiscussionPostWithPerson> = emptyList(),
    val messageReplyTitle: String? = null,
    val loggedInPersonUid: Long = 0L

){

}

class DiscussionPostDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DiscussionPostDetailView.VIEW_NAME,
): DetailViewModel<DiscussionPostWithDetails>(di, savedStateHandle, destinationName){

    private val _uiState = MutableStateFlow(DiscussionPostDetailUiState())

    val uiState: Flow<DiscussionPostDetailUiState> = _uiState.asStateFlow()

    private val postUid = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    var clazzUid: Long = 0L

    init {
        val accountManager: UstadAccountManager by instance()

        val loggedInPersonUid = accountManager.activeSession?.userSession?.usPersonUid ?: 0


        _appUiState.update {prev ->
            prev.copy(
                loadingState = LoadingUiState.INDETERMINATE,
                fabState =  FabUiState(
                    visible = false,

                )
            )
        }

        viewModelScope.launch {

            _uiState.whenSubscribed {
                launch {
                    activeDb.discussionPostDao.findWithDetailsByUidAsFlow(postUid).collect { post ->
                        _uiState.update { prev -> prev.copy(discussionPost = post) }
                        _appUiState.update { prev ->
                            prev.copy(
                                loadingState = if (post != null) {
                                    LoadingUiState.NOT_LOADING
                                } else {
                                    LoadingUiState.INDETERMINATE
                                }
                            )
                        }
                        clazzUid = post?.discussionPostClazzUid ?: 0L
                    }


                }


                launch {
                    activeDb.discussionPostDao.getPostTitleAsFlow(postUid).collect {

                        _appUiState.update { prev ->
                            prev.copy(
                                title = it
                            )
                        }
                    }
                }

                launch {
                    //Get replies as flow:
                    activeDb.discussionPostDao.findAllRepliesByPostUidAsFlow(postUid).collect {
                        _uiState.update { prev -> prev.copy(replies = it) }
                    }
                }
            }
        }
    }


    //onClicks:

    fun onClickEntry(entry: DiscussionPostWithPerson){
        //TODO
    }
    fun onClickDeleteEntry(entry: DiscussionPostWithPerson){
        //TODO
    }

    fun addMessage(message: String) {
        //val postUid: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

        viewModelScope.launch {
            val updateListNeeded = postUid == 0L
            val loggedInPersonUid = accountManager.activeAccount.personUid

            val reply =  DiscussionPost().apply {
                discussionPostArchive = true
                discussionPostClazzUid = clazzUid
                discussionPostStartedPersonUid = loggedInPersonUid
                discussionPostDiscussionTopicUid = postUid
                discussionPostMessage = message
                discussionPostStartDate = systemTimeInMillis()
                discussionPostVisible = true


            }
            activeDb.withDoorTransactionAsync { txRepo ->
                txRepo.discussionPostDao.insertAsync(reply)
            }

            if (updateListNeeded) {

                //Get replies as flow:
                activeDb.discussionPostDao.findAllRepliesByPostUidAsFlow(
                    postUid
                ).collect{
                    _uiState.update { prev -> prev.copy(replies = it) }
                }

            }

        }
    }

    fun updateMessageRead(messageRead: MessageRead){
        viewModelScope.launch {
            activeDb.withDoorTransactionAsync { txRepo ->
                txRepo.messageReadDao.insertAsync(messageRead)
            }
        }
    }


}