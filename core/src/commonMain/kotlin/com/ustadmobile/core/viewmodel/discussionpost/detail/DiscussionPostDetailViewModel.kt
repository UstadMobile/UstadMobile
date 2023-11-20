package com.ustadmobile.core.viewmodel.discussionpost.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class DiscussionPostDetailUiState2(
    val discussionPosts: ListPagingSourceFactory<DiscussionPostAndPosterNames> = {
        EmptyPagingSource()
    },
    val replyText: String = "",
    val loggedInPersonUid: Long = 0L,
    val fieldsEnabled: Boolean = true,
){

}

class DiscussionPostDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): DetailViewModel<DiscussionPostWithDetails>(di, savedStateHandle, destinationName){

    private val pagingSourceFactory: ListPagingSourceFactory<DiscussionPostAndPosterNames> = {
        activeRepo.discussionPostDao.findByPostIdWithAllReplies(entityUidArg).also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, DiscussionPostAndPosterNames>? = null

    private val _uiState = MutableStateFlow(DiscussionPostDetailUiState2())

    val uiState: Flow<DiscussionPostDetailUiState2> = _uiState.asStateFlow()

    private var saveReplyJob: Job? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                discussionPosts = pagingSourceFactory,
                loggedInPersonUid = activeUserPersonUid,
                replyText = savedStateHandle[STATE_KEY_REPLY_TEXT] ?: "",
            )
        }

        viewModelScope.launch {
            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_REPLY_TEXT).collect { result ->
                    val replyText = result.result as? String ?: return@collect
                    submitReply(replyText)
                }
            }

            launch {
                activeRepo.discussionPostDao.getTitleByUidAsFlow(entityUidArg).collect {postTitle ->
                    _appUiState.takeIf { it.value.title != postTitle }?.update { prev ->
                        prev.copy(
                            title = postTitle
                        )
                    }
                }
            }
        }
    }

    fun onChangeReplyText(replyText: String) {
        _uiState.update { prev ->
            prev.copy(replyText = replyText)
        }

        saveReplyJob?.cancel()
        saveReplyJob = viewModelScope.launch {
            delay(200)
            savedStateHandle[STATE_KEY_REPLY_TEXT] = replyText
        }
    }


    //On Android - take the user to a new fullscreen richtext editor
    fun onClickEditReplyHtml() {
        navigateToEditHtml(
            currentValue = _uiState.value.replyText,
            resultKey = RESULT_KEY_REPLY_TEXT,
            extraArgs = mapOf(
                HtmlEditViewModel.ARG_DONE_STR to systemImpl.getString(MR.strings.post),
                HtmlEditViewModel.ARG_TITLE to systemImpl.getString(MR.strings.add_a_reply),
            )
        )
    }

    fun onClickPostReply() {
        viewModelScope.launch {
            submitReply(_uiState.value.replyText)
        }
    }

    private suspend fun submitReply(replyText: String) {
        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }
        loadingState = LoadingUiState.INDETERMINATE

        try {
            //We probably have the course block in the local db, but check just in case. If we don't
            //have it, then load from repo
            val clazzAndBlockUids = activeDb.courseBlockDao.findCourseBlockAndClazzUidByDiscussionPostUid(
                entityUidArg
            ).let {
                //Double check that we
                if(it != null && it.clazzUid != 0L && it.courseBlockUid != 0L)
                    it
                else
                    activeRepo.courseBlockDao.findCourseBlockAndClazzUidByDiscussionPostUid(entityUidArg)
            }

            if(clazzAndBlockUids == null || clazzAndBlockUids.courseBlockUid == 0L ||
                clazzAndBlockUids.clazzUid == 0L
            ) {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
                return
            }

            activeRepo.withDoorTransactionAsync {
                activeRepo.discussionPostDao.insertAsync(DiscussionPost().apply {
                    discussionPostStartDate = systemTimeInMillis()
                    discussionPostReplyToPostUid = entityUidArg
                    discussionPostMessage = replyText
                    discussionPostStartedPersonUid = activeUserPersonUid
                    discussionPostClazzUid = clazzAndBlockUids.clazzUid
                    discussionPostCourseBlockUid = clazzAndBlockUids.courseBlockUid
                })
                onChangeReplyText("")
            }
        }finally {
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = true,
                )
            }
        }
    }

    companion object {

        const val RESULT_KEY_REPLY_TEXT = "replyTextResult"

        const val STATE_KEY_REPLY_TEXT = "replyText"

        const val DEST_NAME = "CourseDiscussionPost"
    }

}