package com.ustadmobile.port.android.view.discussionpost.coursediscussiondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import java.util.*
import androidx.paging.compose.items
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.port.android.view.composable.HtmlText
import com.ustadmobile.port.android.view.composable.UstadListSpacerItem

class CourseDiscussionDetailFragment: UstadBaseMvvmFragment() {

    val viewModel by ustadViewModels(::CourseDiscussionDetailViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    CourseDiscussionDetailScreen(viewModel)
                }
            }
        }
    }

}

@Composable
private fun CourseDiscussionDetailScreen(
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

            HtmlText(
                uiState.courseBlock?.cbDescription?:"",
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
        }

        item{
            Spacer(modifier = Modifier.height(10.dp))
        }

        item{
            Text(stringResource(R.string.posts),
                style = Typography.h4,
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
                                text = stringResource(R.string.num_replies,
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

@Composable
@Preview
fun CourseDiscussionDetailScreenPreview(){
    val uiState = CourseDiscussionDetailUiState(

        courseBlock = CourseBlock().apply{
          cbTitle = "Discussions on Module 4: Statistics and Data Science"
          cbDescription = "Here Any discussion related to Module 4 of Data Science chapter goes here."
        },


        posts = {
            ListPagingSource(listOf(
                DiscussionPostWithDetails().apply {
                    discussionPostTitle = "Can I join after week 2?"
                    discussionPostUid = 0L
                    discussionPostMessage = "Iam late to class, CAn I join after?"
                    discussionPostVisible = true
                    postRepliesCount = 4
                    postLatestMessage = "Just make sure you submit a late assignment."
                    authorPersonFirstNames = "Mike"
                    authorPersonLastName = "Jones"
                    discussionPostStartDate = systemTimeInMillis()
                },
                DiscussionPostWithDetails().apply {
                    discussionPostTitle = "How to install xlib?"
                    discussionPostMessage = "Which version of python do I need?"
                    discussionPostVisible = true
                    discussionPostUid = 1L
                    postRepliesCount = 2
                    postLatestMessage = "I have the same question"
                    authorPersonFirstNames = "Bodium"
                    authorPersonLastName = "Carafe"
                    discussionPostStartDate = systemTimeInMillis()
                }

            ))
        }
    )

    MdcTheme{
        CourseDiscussionDetailScreen(uiState)
    }
}