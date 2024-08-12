package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.libuicompose.components.UstadClickableTextField
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.components.defaultEditHtmlInNewScreen
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun DiscussionPostDetailScreen(viewModel: DiscussionPostDetailViewModel){
    val uiState: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsStateWithLifecycle(
        DiscussionPostDetailUiState2(), Dispatchers.Main.immediate
    )

    DiscussionPostDetailScreen(
        uiState = uiState,
        replyText = viewModel.replyText,
        onClickAddReply = viewModel::onClickEditReplyHtml,
        onChangeReplyText = viewModel::onChangeReplyText,
        onClickReplyButton = viewModel::onClickPostReply,
        onDeletePost = viewModel::onDeletePost,
    )

}
@Composable
fun DiscussionPostDetailScreen(
    uiState: DiscussionPostDetailUiState2,
    replyText: Flow<String>,
    editReplyInNewScreen: Boolean = defaultEditHtmlInNewScreen(),
    onClickAddReply: () -> Unit = { },
    onChangeReplyText: (String) -> Unit = { },
    onClickReplyButton: () -> Unit = { },
    onDeletePost: (DiscussionPost) -> Unit = { },
) {

    val repositoryResult = rememberDoorRepositoryPager(
        uiState.discussionPosts, rememberEmptyFlow()
    )

    val lazyPagingItems = repositoryResult.lazyPagingItems

    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.currentSystemDefault().id)

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.discussionPost?.discussionPostUid ?: 0}
        ) { discussionPostItem ->
            val isRootPost = discussionPostItem?.discussionPost?.discussionPostReplyToPostUid == 0L
            DiscussionPostListItem(
                discussionPost = discussionPostItem,
                showModerateOptions = uiState.showModerateOptions && !isRootPost,
                timeFormat = timeFormatter,
                dateFormat = dateFormatter,
                localDateTimeNow = uiState.localDateTimeNow,
                dayOfWeekStringMap = uiState.dayOfWeekStrings,
                onClickDelete = {
                    discussionPostItem?.discussionPost?.also(onDeletePost)
                }
            )

            //This is the root item - show add a reply here
            if(isRootPost) {
                if(editReplyInNewScreen) {
                    ListItem(
                        headlineContent = {
                            UstadClickableTextField(
                                value = stringResource(MR.strings.add_a_reply) + "…",
                                onValueChange = { },
                                onClick = onClickAddReply,
                                clickableTestTag = "add_a_reply",
                            )
                        },
                        leadingContent = {
                            UstadPersonAvatar(
                                personName = uiState.loggedInPersonName,
                                pictureUri = uiState.loggedInPersonPictureUri,
                            )
                        }
                    )
                }else {
                    val replyTextVal by replyText.collectAsState("", Dispatchers.Main.immediate)

                    UstadRichTextEdit(
                        html = replyTextVal,
                        onHtmlChange = onChangeReplyText,
                        onClickToEditInNewScreen =  { },
                        editInNewScreen = false,
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

                HorizontalDivider(thickness = 1.dp)
            }
        }

        UstadListSpacerItem()
    }
}