package com.ustadmobile.core.viewmodel.discussionpost.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI

data class DiscussionPostDetailUiState2(
    val discussionPosts: ListPagingSourceFactory<DiscussionPostAndPosterNames> = {
        EmptyPagingSource()
    },
    val loggedInPersonUid: Long = 0L,
    val loggedInPersonName: String = "",
    val loggedInPersonPictureUri: String? = null,
    val fieldsEnabled: Boolean = true,
    val showModerateOptions: Boolean = false,
    val localDateTimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()),
    val dayOfWeekStrings: Map<DayOfWeek, String> = emptyMap()
)

class DiscussionPostDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): DetailViewModel<DiscussionPostWithDetails>(di, savedStateHandle, destinationName){

    private val pagingSourceFactory: ListPagingSourceFactory<DiscussionPostAndPosterNames> = {
        activeRepo.discussionPostDao().findByPostIdWithAllReplies(entityUidArg, false)
    }

    private val _uiState = MutableStateFlow(DiscussionPostDetailUiState2())

    val uiState: Flow<DiscussionPostDetailUiState2> = _uiState.asStateFlow()

    private val _replyText = MutableStateFlow("")

    val replyText: Flow<String> = _replyText.asStateFlow()

    private var saveReplyJob: Job? = null

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.coursePermissionDao().personHasPermissionWithClazzPairAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    clazzUid = clazzUid,
                    firstPermission = PermissionFlags.COURSE_VIEW,
                    secondPermission = PermissionFlags.COURSE_MODERATE
                ).distinctUntilChanged().collectLatest { permissionPair ->
                    val (hasViewPermission, hasModeratePermission) = permissionPair

                    if(hasViewPermission) {
                        _uiState.update { prev ->
                            prev.copy(
                                discussionPosts = pagingSourceFactory,
                                loggedInPersonUid = activeUserPersonUid,
                                loggedInPersonName = accountManager.currentUserSession.person.fullName(),
                                loggedInPersonPictureUri = accountManager.currentUserSession.personPicture?.personPictureThumbnailUri,
                                showModerateOptions = hasModeratePermission,
                            )
                        }

                        _replyText.value = savedStateHandle[STATE_KEY_REPLY_TEXT] ?: ""

                        launch {
                            resultReturner.filteredResultFlowForKey(RESULT_KEY_REPLY_TEXT).collect { result ->
                                val replyText = result.result as? String ?: return@collect
                                submitReply(replyText)
                            }
                        }

                        launch {
                            activeRepo.discussionPostDao().getTitleByUidAsFlow(entityUidArg).collect {postTitle ->
                                _appUiState.takeIf { it.value.title != postTitle }?.update { prev ->
                                    prev.copy(
                                        title = postTitle
                                    )
                                }
                            }
                        }
                    }else {
                        _uiState.update { prev ->
                            prev.copy(
                                discussionPosts = {  EmptyPagingSource() },
                                loggedInPersonUid = activeUserPersonUid,
                                loggedInPersonName = accountManager.currentUserSession.person.fullName(),
                                loggedInPersonPictureUri = accountManager.currentUserSession.personPicture?.personPictureThumbnailUri,
                                showModerateOptions = false,
                            )
                        }
                        _replyText.value = savedStateHandle[STATE_KEY_REPLY_TEXT] ?: ""
                    }
                }
            }
        }

    }

    fun onChangeReplyText(replyText: String) {
        _replyText.value = replyText

        saveReplyJob?.cancel()
        saveReplyJob = viewModelScope.launch {
            delay(200)
            savedStateHandle[STATE_KEY_REPLY_TEXT] = replyText
        }
    }


    //On Android - take the user to a new fullscreen richtext editor
    fun onClickEditReplyHtml() {
        navigateToEditHtml(
            currentValue = _replyText.value,
            resultKey = RESULT_KEY_REPLY_TEXT,
            extraArgs = mapOf(
                HtmlEditViewModel.ARG_DONE_STR to systemImpl.getString(MR.strings.post),
                HtmlEditViewModel.ARG_TITLE to systemImpl.getString(MR.strings.add_a_reply),
            )
        )
    }

    fun onClickPostReply() {
        viewModelScope.launch {
            submitReply(_replyText.value)
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
            val clazzAndBlockUids = activeDb.courseBlockDao().findCourseBlockAndClazzUidByDiscussionPostUid(
                entityUidArg
            ).let {
                //Double check that we
                if(it != null && it.clazzUid != 0L && it.courseBlockUid != 0L)
                    it
                else
                    activeRepo.courseBlockDao().findCourseBlockAndClazzUidByDiscussionPostUid(entityUidArg)
            }

            if(clazzAndBlockUids == null || clazzAndBlockUids.courseBlockUid == 0L ||
                clazzAndBlockUids.clazzUid == 0L
            ) {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
                return
            }

            activeRepo.withDoorTransactionAsync {
                activeRepo.discussionPostDao().insertAsync(DiscussionPost().apply {
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

    fun onDeletePost(post: DiscussionPost) {
        viewModelScope.launch {
            activeRepo.discussionPostDao().setDeletedAsync(
                uid = post.discussionPostUid, deleted = true, updateTime = systemTimeInMillis()
            )
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.deleted)))
        }
    }

    companion object {

        const val RESULT_KEY_REPLY_TEXT = "replyTextResult"

        const val STATE_KEY_REPLY_TEXT = "replyText"

        const val DEST_NAME = "CourseDiscussionPost"
    }

}