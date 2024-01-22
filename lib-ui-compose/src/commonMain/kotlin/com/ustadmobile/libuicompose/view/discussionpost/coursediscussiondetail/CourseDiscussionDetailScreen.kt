package com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ReplyAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import java.util.*
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.libuicompose.components.UstadHtmlText
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun CourseDiscussionDetailScreen(
    viewModel: CourseDiscussionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        CourseDiscussionDetailUiState(), Dispatchers.Main.immediate)


    CourseDiscussionDetailScreen(
        uiState = uiState,
        onClickPost = viewModel::onClickPost,
    )


}

@Composable
fun CourseDiscussionDetailScreen(
    uiState: CourseDiscussionDetailUiState = CourseDiscussionDetailUiState(),
    onClickPost: (DiscussionPostWithDetails) -> Unit = {},
){

    val pager = remember(uiState.posts) {
        Pager(
            pagingSourceFactory = uiState.posts,
            config = PagingConfig(pageSize = 50, enablePlaceholders = true)
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val systemTimeZone = remember {
        TimeZone.getDefault().id
    }

    LazyColumn{
        item(key = "description"){
            UstadHtmlText(
                uiState.courseBlock?.cbDescription?:"",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
        }


        item(key = "postsheader"){
            UstadDetailHeader {
                Text(
                    text = stringResource(MR.strings.posts),
                )
            }
        }

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { post -> post.discussionPostUid }
        ) { discussionPostItem ->
            CourseDiscussionDetailDiscussionListItem(
                discussionPostItem = discussionPostItem,
                systemTimeZone = systemTimeZone,
                onClickPost = onClickPost
            )
        }

        UstadListSpacerItem()

    }
}

@Composable
fun CourseDiscussionDetailDiscussionListItem(
    discussionPostItem: DiscussionPostWithDetails?,
    systemTimeZone: String,
    onClickPost: (DiscussionPostWithDetails) -> Unit = { },
) {
    val datePosted = rememberFormattedDateTime(
        timeInMillis = discussionPostItem?.discussionPostStartDate ?: 0,
        timeZoneId = systemTimeZone,
        joinDateAndTime = {date, time -> "$date\n$time"}
    )

    val messageText = remember(
        discussionPostItem?.postLatestMessage,
        discussionPostItem?.discussionPostMessage
    ) {
        val html = discussionPostItem?.postLatestMessage ?: discussionPostItem?.discussionPostMessage ?: ""
        html.htmlToPlainText()
    }

    val authorName = "${discussionPostItem?.authorPersonFirstNames} ${discussionPostItem?.authorPersonLastName}"

    ListItem(
        modifier = Modifier.clickable {
            discussionPostItem?.also(onClickPost)
        },
        leadingContent = {
            UstadPersonAvatar(
                personName = authorName,
                pictureUri = discussionPostItem?.authorPictureUri
            )
        },
        headlineContent = {
            Text(
                text = authorName,
                maxLines = 1,
            )
        },

        supportingContent = {
            Column {
                Text(
                    text = discussionPostItem?.discussionPostTitle ?:"",
                    maxLines = 1,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    //style = MaterialTheme.typography.subtitle1,
                )

                Row {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "",
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = messageText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }

                Row {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Filled.ReplyAll,
                        contentDescription = null,
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = stringResource(MR.strings.num_replies,
                            discussionPostItem?.postRepliesCount ?: 0)
                    )
                }
            }
        },
        trailingContent = {
            Column {
                Text(
                    text = datePosted
                )
            }
        }
    )
}