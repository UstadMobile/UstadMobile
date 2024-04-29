package com.ustadmobile.view.discussionpost.coursediscussiondetail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import mui.material.*
import mui.material.List
import mui.system.sx
import react.*

external interface CourseDiscussionDetailProps: Props {
    var uiState: CourseDiscussionDetailUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onClickPost: (DiscussionPostWithDetails) -> Unit
    var onDeletePost: (DiscussionPost) -> Unit
}

val CourseDiscussionDetailComponent = FC<CourseDiscussionDetailProps> { props ->
    val theme by useRequiredContext(ThemeContext)

    val emptyRefreshFlow = useEmptyFlow<RefreshCommand>()

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.posts, props.refreshCommandFlow ?: emptyRefreshFlow
    )

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = mediatorResult.pagingSourceFactory,
        placeholdersEnabled = true
    )

    val muiAppState = useMuiAppState()

    val timeFormatter = useTimeFormatter()
    val dateFormatter = useDateFormatter()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item(key = "header") {
                UstadCourseBlockHeader.create {
                    sx {
                        paddingTop = theme.spacing(1)
                    }

                    block = props.uiState.courseBlock?.block
                    picture = props.uiState.courseBlock?.picture
                }
            }
            item(key = "description") {
                UstadRawHtml.create {
                    html = props.uiState.courseBlock?.block?.cbDescription ?: ""
                }
            }
            item(key = "divider") {
                Divider.create()
            }
            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.discussionPostUid.toString() }
            ) { discussionPostItem ->
                CourseDiscussionDetailPostListItem.create {
                    discussionPost = discussionPostItem
                    onClick = {
                        discussionPostItem?.also(props.onClickPost)
                    }
                    onClickDelete = {
                        discussionPostItem?.also(props.onDeletePost)
                    }
                    showModerateOptions = props.uiState.showModerateOptions
                    localDateTimeNow = props.uiState.localDateTimeNow
                    timeFormat = timeFormatter
                    dateFormat = dateFormatter
                    dayOfWeekStrings = props.uiState.dayOfWeekStrings
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}

val CourseDiscussionDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseDiscussionDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CourseDiscussionDetailUiState())
    val appUiState by viewModel.appUiState.collectAsState(AppUiState())

    CourseDiscussionDetailComponent {
        uiState = uiStateVal
        onClickPost = viewModel::onClickPost
        onDeletePost = viewModel::onDeletePost
    }

    UstadFab {
        fabState = appUiState.fabState
    }

}
