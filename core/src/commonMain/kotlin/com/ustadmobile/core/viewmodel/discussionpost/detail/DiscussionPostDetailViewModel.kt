package com.ustadmobile.core.viewmodel.discussionpost.detail

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class DiscussionPostDetailUiState(
    val discussionPost: DiscussionPostWithDetails? = null,
    val replies: List<DiscussionPostWithPerson> = emptyList(),
    var messageReplyTitle: String? = null,
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
                    println("POTATO3")
                    activeDb.discussionPostDao.findAllRepliesByPostUidAsFlow(postUid).collect {
                        _uiState.update { prev -> prev.copy(replies = it) }
                    }
                }

                launch {
                    resultReturner.filteredResultFlowForKey(RESULT_KEY_HTML_DESC).collect { result ->
                        val newMessage = result.result as? String ?: return@collect
                        _uiState.value.messageReplyTitle = newMessage
                        onEntityChanged(_uiState.value.discussionPost, newMessage)
                    }
                }

            }



        }
    }

    fun onEntityChanged(entity: DiscussionPostWithDetails?, newMessage: String?){
        _uiState.update {prev ->
            prev.copy(
                discussionPost = entity,
                messageReplyTitle = newMessage,
            )
        }


    }

    //onClicks:

    fun onClickEntry(entry: DiscussionPostWithPerson){
        //TODO
    }

    fun onClickEditReply(){
        navigateToEditHtml(
            currentValue = _uiState.value.messageReplyTitle,
            resultKey = RESULT_KEY_HTML_DESC
        )
    }


    fun onClickDeleteEntry(entry: DiscussionPostWithPerson){
        viewModelScope.launch {
            entry.discussionPostVisible = false
            activeDb.discussionPostDao.updateAsync(entry)
        }
    }

    fun addMessageD(){
        val message = _uiState.value.messageReplyTitle
        if(!message.isNullOrEmpty()){
            _uiState.value.messageReplyTitle = ""
            addMessage(message)
        }
    }
    fun addMessage(message: String) {

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