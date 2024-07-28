package com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.launchSetTitleFromClazzUid
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndPicture
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI

data class CourseDiscussionDetailUiState(
    val courseBlock: CourseBlockAndPicture? = null,
    val posts: ListPagingSourceFactory<DiscussionPostWithDetails> = { EmptyPagingSource() },
    val showModerateOptions: Boolean = false,
    val localDateTimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()),
    val dayOfWeekStrings: Map<DayOfWeek, String> = emptyMap()
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

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    private val pagingSourceFactory : ListPagingSourceFactory<DiscussionPostWithDetails> = {
        activeRepo.discussionPostDao().getTopLevelPostsByCourseBlockUid(courseBlockUid, false)
    }

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
                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = FabUiState(
                                    visible = true,
                                    text = systemImpl.getString(MR.strings.post),
                                    icon = FabUiState.FabIcon.ADD,
                                    onClick = this@CourseDiscussionDetailViewModel::onClickAdd
                                )
                            )
                        }

                        _uiState.update { prev ->
                            prev.copy(
                                posts = pagingSourceFactory,
                                showModerateOptions = hasModeratePermission,
                                dayOfWeekStrings = systemImpl.getDayOfWeekStrings(),
                            )
                        }

                        activeRepo.courseBlockDao().findByUidWithPictureAsFlow(courseBlockUid).collect {
                            _uiState.update { prev ->
                                prev.copy(courseBlock = it)
                            }
                        }
                    }else{
                        _appUiState.update { prev -> prev.copy(fabState = FabUiState()) }
                        _uiState.update { prev ->
                            prev.copy(
                                courseBlock = null,
                                posts = { EmptyPagingSource() },
                                showModerateOptions = false,
                            )
                        }
                    }
                }
            }
        }

        launchSetTitleFromClazzUid(clazzUid) { title ->
            _appUiState.update { it.copy(title = title) }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        navigateToCreateNew(
            DiscussionPostEditViewModel.DEST_NAME,
            extraArgs = mapOf(
                ARG_COURSE_BLOCK_UID to courseBlockUid.toString(),
                ARG_CLAZZUID to clazzUid.toString(),
            )
        )
    }

    fun onClickPost(post: DiscussionPostWithDetails) {
        navController.navigate(
            viewName = DiscussionPostDetailViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_ENTITY_UID to post.discussionPostUid.toString(),
                ARG_CLAZZUID to clazzUid.toString(),
            )
        )
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

        const val DEST_NAME = "CourseDiscussion"

    }
}