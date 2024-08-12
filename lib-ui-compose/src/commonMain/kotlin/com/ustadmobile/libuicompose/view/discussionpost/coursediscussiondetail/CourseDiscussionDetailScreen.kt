package com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReplyAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.*
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.libuicompose.components.UstadHtmlText
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.libuicompose.components.UstadCourseBlockHeader
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.text.DateFormat

@Composable
fun CourseDiscussionDetailScreen(
    viewModel: CourseDiscussionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        CourseDiscussionDetailUiState(), Dispatchers.Main.immediate)


    CourseDiscussionDetailScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickPost = viewModel::onClickPost,
        onDeletePost = viewModel::onDeletePost,
    )


}

@Composable
fun CourseDiscussionDetailScreen(
    uiState: CourseDiscussionDetailUiState = CourseDiscussionDetailUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickPost: (DiscussionPostWithDetails) -> Unit = { },
    onDeletePost: (DiscussionPost) -> Unit = { },
){

    val repositoryResult = rememberDoorRepositoryPager(
        uiState.posts, refreshCommandFlow
    )

    val lazyPagingItems = repositoryResult.lazyPagingItems

    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    UstadLazyColumn{
        item(key = "header") {
            UstadCourseBlockHeader(
                block = uiState.courseBlock?.block,
                picture = uiState.courseBlock?.picture,
                modifier = Modifier.defaultItemPadding(top = 16.dp).fillMaxWidth(),
            )
        }

        item(key = "description"){
            UstadHtmlText(
                uiState.courseBlock?.block?.cbDescription?:"",
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
                onClickPost = onClickPost,
                timeFormatter = timeFormatter,
                dateFormatter = dateFormatter,
                dayOfWeekStringMap = uiState.dayOfWeekStrings,
                localDateTimeNow = uiState.localDateTimeNow,
                showModerateOptions = uiState.showModerateOptions,
                onDeletePost = onDeletePost,
            )
        }

        UstadListSpacerItem()

    }
}

@Composable
fun CourseDiscussionDetailDiscussionListItem(
    discussionPostItem: DiscussionPostWithDetails?,
    localDateTimeNow: LocalDateTime,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    onClickPost: (DiscussionPostWithDetails) -> Unit = { },
    showModerateOptions: Boolean = false,
    onDeletePost: (DiscussionPost) -> Unit = { },
) {
    val dayPosted = rememberDayOrDate(
        localDateTimeNow = localDateTimeNow,
        timestamp = discussionPostItem?.discussionPostStartDate ?: 0,
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormatter,
        dayOfWeekStringMap = dayOfWeekStringMap,
    )

    val messageText = remember(
        discussionPostItem?.postLatestMessage,
        discussionPostItem?.discussionPostMessage
    ) {
        val html = discussionPostItem?.postLatestMessage ?: discussionPostItem?.discussionPostMessage ?: ""
        html.htmlToPlainText()
    }

    val authorName = "${discussionPostItem?.authorPersonFirstNames} ${discussionPostItem?.authorPersonLastName}"

    var optionsExpanded by remember {
        mutableStateOf(false)
    }

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
                        imageVector = Icons.AutoMirrored.Filled.Chat,
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
                        imageVector = Icons.AutoMirrored.Filled.ReplyAll,
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
            Row {
                Text(text = dayPosted)
                if(showModerateOptions) {
                    IconButton(
                        onClick = { optionsExpanded = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(MR.strings.more_options))
                    }

                    DropdownMenu(
                        expanded = optionsExpanded,
                        onDismissRequest = { optionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(MR.strings.delete)) },
                            onClick = {
                                optionsExpanded = false
                                discussionPostItem?.also(onDeletePost)
                            }
                        )
                    }
                }
            }
        }
    )
}