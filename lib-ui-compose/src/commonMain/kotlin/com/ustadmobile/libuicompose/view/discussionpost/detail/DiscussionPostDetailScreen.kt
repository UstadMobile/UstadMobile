package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.libuicompose.components.UstadClickableTextField
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.defaultEditHtmlInNewScreen
import com.ustadmobile.libuicompose.components.ustadPagedItems
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadHtmlEditPlaceholder
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun DiscussionPostDetailScreen(viewModel: DiscussionPostDetailViewModel){
    val uiState: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsStateWithLifecycle(
        DiscussionPostDetailUiState2(), Dispatchers.Main.immediate
    )

    DiscussionPostDetailScreen(
        uiState = uiState,
        onClickAddReply = viewModel::onClickEditReplyHtml,
        onChangeReplyText = viewModel::onChangeReplyText,
        onClickReplyButton = viewModel::onClickPostReply
    )

}
@Composable
fun DiscussionPostDetailScreen(
    uiState: DiscussionPostDetailUiState2,
    editReplyInNewScreen: Boolean = defaultEditHtmlInNewScreen(),
    onClickAddReply: () -> Unit = { },
    onChangeReplyText: (String) -> Unit = { },
    onClickReplyButton: () -> Unit = { },
) {

    val pager = remember(uiState.discussionPosts) {
        Pager(
            pagingSourceFactory = uiState.discussionPosts,
            config = PagingConfig(pageSize = 50, enablePlaceholders = true)
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.discussionPost?.discussionPostUid ?: 0}
        ) { discussionPostItem ->
            DiscussionPostListItem(
                discussionPost = discussionPostItem
            )

            //This is the root item - show add a reply here
            if(discussionPostItem?.discussionPost?.discussionPostReplyToPostUid == 0L) {
                if(editReplyInNewScreen) {
                    ListItem(
                        headlineContent = {
                            UstadClickableTextField(
                                value = stringResource(MR.strings.add_a_reply) + "…",
                                onValueChange = { },
                                onClick = onClickAddReply
                            )
                        },
                        leadingContent = {
                            UstadPersonAvatar(
                                personUid = uiState.loggedInPersonUid
                            )
                        }
                    )
                }else {
                    UstadHtmlEditPlaceholder(
                        htmlTextTmp = uiState.replyText,
                        onChangeHtmlTmp = onChangeReplyText,
                        editInNewScreenTmp = false,
                        placeholderText = stringResource(MR.strings.add_a_reply),
                        modifier = Modifier.defaultItemPadding()
                            .fillMaxWidth()
                            .testTag("reply_text")
                    )

                    Button(
                        onClick = onClickReplyButton,
                        modifier = Modifier.fillMaxWidth()
                            .defaultItemPadding()
                            .testTag("post_reply_button"),
                    ) {
                        Text(stringResource(MR.strings.post))
                    }

                }

                Divider(
                    thickness = 1.dp
                )
            }
        }

        UstadListSpacerItem()
    }
}