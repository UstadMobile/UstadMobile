package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.libuicompose.components.UstadListSpacerItem

@Composable
fun DiscussionPostDetailScreenForViewModel(viewModel: DiscussionPostDetailViewModel){
    val uiState: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsState(
        DiscussionPostDetailUiState2()
    )


    DiscussionPostDetailScreen(
        uiState = uiState,
        onClickAddReply = viewModel::onClickEditReplyHtml
    )


}
@Composable
fun DiscussionPostDetailScreen(
    uiState: DiscussionPostDetailUiState2 = DiscussionPostDetailUiState2(),
    onClickAddReply: () -> Unit = { },
) {

//    val pager = remember(uiState.discussionPosts) {
//        Pager(
//            pagingSourceFactory = uiState.discussionPosts,
//            config = PagingConfig(pageSize = 50, enablePlaceholders = true)
//        )
//    }
//
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
//        items(
//            items = lazyPagingItems,
//            key = { it.discussionPost?.discussionPostUid ?: 0}
//        ) { discussionPostItem ->
//            DiscussionPostListItem(
//                discussionPost =discussionPostItem
//            )
//
//            //This is the root item - show add a reply here
//            if(discussionPostItem?.discussionPost?.discussionPostReplyToPostUid == 0L) {
//                UstadAddCommentListItem(
//                    text = stringResource(MR.strings.add_a_reply),
//                    personUid = uiState.loggedInPersonUid,
//                    onClickAddComment =  onClickAddReply,
//                )
//
//                Divider(
//                    thickness = 1.dp
//                )
//            }
//        }
//
        UstadListSpacerItem()
    }
}