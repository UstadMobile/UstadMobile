package com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CourseDiscussionDetailUiState(
    val courseBlock: CourseBlock? = null,
    val posts: ListPagingSourceFactory<DiscussionPostWithDetails> = { EmptyPagingSource() },
)

/**
 * CourseDiscussionDetailViewModel will show all the top level posts eg. DiscussionPost where
 * replyTo = 0 for a given courseBlockUid. This is where the user comes when they click a discussion
 * from the course.
 */
class CourseDiscussionDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<CourseDiscussionDetailUiState>(
    di, savedStateHandle, CourseDiscussionDetailUiState(), DEST_NAME
) {

    private val courseBlockUid = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

    private val pagingSourceFactory : ListPagingSourceFactory<DiscussionPostWithDetails> = {
        activeRepo.discussionPostDao.getTopLevelPostsByCourseBlockUid(courseBlockUid)
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.post),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = this::onClickAdd
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(
                posts = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.courseBlockDao.findByUidAsFlow(courseBlockUid).collect {
                        _uiState.update { prev ->
                            prev.copy(courseBlock = it)
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                title = it?.cbTitle
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        navigateToCreateNew(
            DiscussionPostEditViewModel.DEST_NAME,
            extraArgs = mapOf(
                ARG_COURSE_BLOCK_UID to courseBlockUid.toString(),
            )
        )
    }

    fun onClickPost(post: DiscussionPostWithDetails) {
        navController.navigate(
            viewName = DiscussionPostDetailViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_ENTITY_UID to post.discussionPostUid.toString()
            )
        )
    }

    companion object {

        const val DEST_NAME = "CourseDiscussion"

    }
}