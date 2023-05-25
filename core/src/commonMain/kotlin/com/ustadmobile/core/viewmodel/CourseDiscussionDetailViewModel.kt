package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CourseDiscussionDetailUiState(
    val courseBlock: CourseBlock? = null,
    val posts: ListPagingSourceFactory<DiscussionPostWithDetails> = { EmptyPagingSource() },
)

/**
 * CourseDiscussionDetailViewModel will show all the top level posts eg. DiscussionPost where replyTo = 0.
 */
class CourseDiscussionDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<CourseDiscussionDetailUiState>(di, savedStateHandle, CourseDiscussionDetailUiState(), DEST_NAME) {

    private val courseBlockUid = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

    private val pagingSourceFactory : ListPagingSourceFactory<DiscussionPostWithDetails> = {
        activeRepo.discussionPostDao.getTopLevelPostsByCourseBlockUid(courseBlockUid)
    }

    init {
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
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {

    }

    companion object {

        const val DEST_NAME = "CourseDiscussion"

    }
}