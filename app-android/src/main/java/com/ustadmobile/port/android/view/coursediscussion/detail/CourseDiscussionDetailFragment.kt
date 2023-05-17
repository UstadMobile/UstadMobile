package com.ustadmobile.port.android.view.coursediscussion.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.coursediscussion.detail.CourseDiscussionDetailUiState
import com.ustadmobile.core.viewmodel.coursediscussion.detail.CourseDiscussionDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseBlock
import androidx.paging.compose.items
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.util.ext.defaultAvatarSize
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.HtmlText
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar

import java.util.*



class CourseDiscussionDetailFragment(): UstadBaseMvvmFragment(){

    private val viewModel: CourseDiscussionDetailViewModel by ustadViewModels{
        di, savedStateHandle ->
        CourseDiscussionDetailViewModel(
            di, savedStateHandle, requireDestinationViewName()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    CourseDiscussionDetailFragmentScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun CourseDiscussionDetailFragmentScreen(viewModel: CourseDiscussionDetailViewModel){
    val uiState: CourseDiscussionDetailUiState by viewModel.uiState.collectAsState(
        CourseDiscussionDetailUiState()
    )

    val context = LocalContext.current
    CourseDiscussionDetailScreen(
        uiState = uiState,
        onClickAddPost = viewModel::onClickAdd,
        onClickPost = viewModel::onClick,
        onClickDeletePost = viewModel::onClickDeleteEntry
    )

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CourseDiscussionDetailScreen(
    uiState: CourseDiscussionDetailUiState = CourseDiscussionDetailUiState(),
    onClickAddPost: () -> Unit = {},
    onClickPost: (DiscussionPostWithDetails) -> Unit = {},
    onClickDeletePost: (DiscussionPostWithDetails) -> Unit = {},
){

    val pager = remember(uiState.posts){
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.posts
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        //Description:
        item{
            HtmlText(
                html = uiState.courseBlock?.cbDescription?:"",
                modifier = Modifier.padding(8.dp)
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
        ){ post ->

            val datePosted = rememberFormattedDate( timeInMillis = post?.discussionPostStartDate?:0,
                "UTC" )

            ListItem(
                modifier = Modifier.clickable {
                    post?.also{
                        onClickPost(it)
                    }

                },
                icon = {
                    UstadPersonAvatar(
                        post?.discussionPostStartedPersonUid ?: 0,
                        modifier = Modifier.defaultAvatarSize(),
                    )

                },
                text = {
                    Text(post?.authorPersonFirstNames + " " + post?.authorPersonLastName ?: "" )
                },

                secondaryText = {
                    Column {
                        HtmlText(post?.discussionPostTitle?: "")
                    }
                },
                singleLineSecondaryText = false,
                trailing = {
                    Column {
                        Text(datePosted)
                        Text(stringResource(R.string.num_replies, post?.postRepliesCount?:0))
                    }
                }
            )
        }

    }

}

@Composable
@Preview
fun CourseDiscussionDetailScreenPreview(){

    val postAsList = listOf(
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

    )
    val uiState = CourseDiscussionDetailUiState(

        courseBlock = CourseBlock().apply{
          cbTitle = "Discussions on Module 4: Statistics and Data Science"
          cbDescription = "Here Any discussion related to Module 4 of Data Science chapter goes here."
        },


        posts = { ListPagingSource(postAsList) } ,

        )

    MdcTheme{
        CourseDiscussionDetailScreen(uiState)
    }
}