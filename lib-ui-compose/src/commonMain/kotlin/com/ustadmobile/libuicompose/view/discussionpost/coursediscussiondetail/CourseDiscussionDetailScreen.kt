package com.ustadmobile.port.android.view.discussionpost.coursediscussiondetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReplyAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import java.util.*
import androidx.paging.compose.items
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadListSpacerItem

@Composable
fun CourseDiscussionDetailScreenForViewModel(
    viewModel: CourseDiscussionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseDiscussionDetailUiState())
    CourseDiscussionDetailScreen(
        uiState = uiState,
        onClickPost = viewModel::onClickPost,
    )

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CourseDiscussionDetailScreen(
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

        //Description:
        item{

            // TODO error
//            HtmlText(
//                uiState.courseBlock?.cbDescription?:"",
//                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
//            )
        }

        item{
            Spacer(modifier = Modifier.height(10.dp))
        }

        item{
            Text(stringResource(MR.strings.posts),
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(8.dp))
        }


        items(
            items = lazyPagingItems,
            key = { post -> post.discussionPostUid }
        ){ discussionPostItem ->

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

            ListItem(
                modifier = Modifier.clickable {
                    discussionPostItem?.also(onClickPost)
                },
                icon = {
                    //TODO: replace with avatar
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null
                    )
                },
                text = {
                    Text("${discussionPostItem?.authorPersonFirstNames} ${discussionPostItem?.authorPersonLastName}" )
                },

                secondaryText = {
                    Column {
                        Text(
                            text = discussionPostItem?.discussionPostTitle ?:"",
                            style = MaterialTheme.typography.subtitle1,
                        )

                        Row {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "",
                            )

                            Text(
                                text = messageText,
                                style = MaterialTheme.typography.body1,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }

                        Row {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Filled.ReplyAll,
                                contentDescription = "",
                            )

                            Text(
                                text = stringResource(MR.strings.num_replies,
                                    discussionPostItem?.postRepliesCount ?: 0)
                            )
                        }
                    }
                },
                singleLineSecondaryText = false,
                trailing = {
                    Column {
                        Text(
                            text = datePosted
                        )
                    }
                }
            )
        }

        UstadListSpacerItem()

    }

}