package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_BLOCK_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class CourseDiscussionDetailUiState(
    val courseBlock: CourseBlock? = null,
    val posts: () -> PagingSource<Int, DiscussionPostWithDetails> = { EmptyPagingSource() },
    val loggedInPersonUid: Long = 0L
    ){

}

class CourseDiscussionDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = CourseDiscussionDetailView.VIEW_NAME,
):UstadListViewModel<CourseDiscussionDetailUiState>(di, savedStateHandle, CourseDiscussionDetailUiState(), destinationName){

    private var lastPagingSource: PagingSource<Int, DiscussionPostWithDetails>? = null

    private val pagingSourceFactory:() -> PagingSource<Int, DiscussionPostWithDetails> = {
        activeRepo.discussionPostDao.findAllPostsByCourseBlockAsPagingSource(courseBlockUid,
            searchQuery =_appUiState.value.searchState.searchText.toQueryLikeParam()).also {
                lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }
    override fun onUpdateSearchResult(searchText: String) {
        lastPagingSource?.invalidate()
    }

    val courseBlockUid: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong()?: 0

    var clazzUid: Long =  savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()?: 0

    init {
        val accountManager: UstadAccountManager by instance()

        val loggedInPersonUid = accountManager.activeSession?.userSession?.usPersonUid ?: 0

        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MessageID.posts, MessageID.post),
                fabState = createFabState(true, MessageID.post)
            )
        }
        _uiState.update { prev ->
            prev.copy(
                posts = pagingSourceFactory,
                loggedInPersonUid = loggedInPersonUid
            )
        }

        viewModelScope.launch {
            //Get the block details
            _uiState.whenSubscribed {
                launch {
                    activeDb.courseBlockDao.findByUidFlow(courseBlockUid).collect{
                        block ->
                        _uiState.update { prev -> prev.copy(courseBlock = block) }
                        clazzUid = block?.cbClazzUid?:0L
                        title = block?.cbTitle?:""

                    }

                }
            }

        }
    }

    fun onClick(entry: DiscussionPostWithDetails){
        navigateOnItemClicked(DiscussionPostDetailView.VIEW_NAME, entry.discussionPostUid, entry)
    }

    fun onClickDeleteEntry(entry: DiscussionPostWithDetails){
        //TODO
    }

    override fun onClickAdd(){
        navController.navigate(
            DiscussionPostEditView.VIEW_NAME,
            mapOf(
                ARG_BLOCK_UID to courseBlockUid.toString(),
                ARG_CLAZZUID to clazzUid.toString()
            )
        )

        //navigateToCreateNew(DiscussionPostEditView.VIEW_NAME)
    }

}

