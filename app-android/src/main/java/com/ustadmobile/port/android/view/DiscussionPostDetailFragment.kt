package com.ustadmobile.port.android.view


import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.DiscussionPostDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionPostWithPerson
import com.ustadmobile.lib.db.entities.Person
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import java.util.*


class DiscussionPostDetailFragment: UstadBaseMvvmFragment() {

    private val viewModel: DiscussionPostDetailViewModel by ustadViewModels()

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
                    DiscussionPostDetailFragmentScreen(viewModel)
                }
            }
        }
    }


    companion object {
        //If anything..
    }

}

@Composable
fun DiscussionPostDetailFragmentScreen(viewModel: DiscussionPostDetailViewModel){
    val uiState: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsState(DiscussionPostDetailUiState2())

    val context = LocalContext.current

    DiscussionPostDetailFragmentScreen(
        uiState = uiState,
        onClickAddMessage = viewModel::addMessage,
        onClickDeleteMessage = viewModel::onClickDeleteEntry,
        onClickMessage = viewModel::onClickEntry
    )


}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DiscussionPostDetailFragmentScreen(
    uiState: DiscussionPostDetailUiState2 = DiscussionPostDetailUiState2(),
    onClickDeleteMessage: (DiscussionPostWithPerson) -> Unit = {},
    onClickMessage: (DiscussionPostWithPerson) -> Unit = {},
    onClickAddMessage: (String) -> Unit = {},
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        val fullName= uiState.discussionPost?.authorPersonFirstNames + " " + uiState.discussionPost?.authorPersonLastName


        val context = LocalContext.current
        val datePosted = remember { DateFormat.getDateFormat(context)
            .format(Date(uiState.discussionPost?.discussionPostStartDate ?: 0)).toString() }


        Text(
            uiState.discussionPost?.discussionPostTitle ?: "",
            style = Typography.body1,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        ListItem(
            modifier = Modifier.clickable {
                //onClickPost(post)
            },
            icon = {
                //TODO: replace with avatar
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            text = {
                Text(fullName ?: "" )
            },

            secondaryText = {
                Column {
                    Text(uiState.discussionPost?.discussionPostMessage?: "")
                }
            },
            singleLineSecondaryText = false,
            trailing = {
                Column {
                    Text(datePosted)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            stringResource(R.string.messages),
            style = Typography.h4,
            modifier = Modifier.padding(8.dp)
        )


        //TODO: Figure how to save to new comment thingi
        UstadTextEditField(
            value = "",
            label = stringResource(id = R.string.add_a_reply),
            error = uiState.messageReplyTitle,
            enabled = true,
            onValueChange = {
//                onContentChanged(uiState.discussionPost?.shallowCopy {
//                    discussionPostTitle = it
//                })
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            Messages(uiState.replies, onClickMessage)
        }
    }

}

@Composable
private fun Messages(
    replies: List<DiscussionPostWithPerson> = emptyList(),
    onClickMessage: (DiscussionPostWithPerson) -> Unit = {}
){

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = LazyListState(),
    ) {
        itemsIndexed(replies) { _, item ->
            MessageItem(item)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageItem(post: DiscussionPostWithPerson){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        val context = LocalContext.current
        val datePosted = remember { DateFormat.getDateFormat(context)
            .format(Date(post.discussionPostStartDate ?: 0)).toString() }

        val fullName= post.replyPerson?.fullName() ?: ""


        ListItem(
            modifier = Modifier.clickable {
                //onClickPost(post)
            },
            icon = {
                //TODO: replace with avatar
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            text = {
                Text(fullName ?: "" )
            },

            secondaryText = {
                Column {
                    Text(post.discussionPostMessage?: "")
                }
            },
            singleLineSecondaryText = false,
            trailing = {
                Column {
                    Text(datePosted)
                }
            }
        )



    }
}











@Composable
@Preview
fun DiscussionPostDetailScreenFragmentPreview(){
    val uiState = DiscussionPostDetailUiState2(
        discussionPost = DiscussionPostWithDetails().apply {
            discussionPostTitle = "Submitting an assignment"
            authorPersonFirstNames = "Mohammed"
            authorPersonLastName = "Iqbaal"
            discussionPostVisible = true
            discussionPostMessage = "Hi everyone, cna I get some help in how to submit the assignemnt?"
            discussionPostStartDate = systemTimeInMillis()

        },
        replies = listOf(
            DiscussionPostWithPerson().apply {

                discussionPostMessage = "I have the same question on Android"
                replyPerson = Person().apply {
                    firstNames = "Chahid"
                    lastName = "Dabir"
                    personUid = 2

                }
            },
            DiscussionPostWithPerson().apply {
                discussionPostMessage = "I think it is briefly explained in section 42"
                replyPerson = Person().apply {
                    firstNames = "Daanesh"
                    lastName = "Dabish"
                    personUid = 3

                }
            },

            DiscussionPostWithPerson().apply {
                discussionPostMessage = "Thanks everyone, I got it working now on Android!"
                replyPerson = Person().apply {
                    firstNames = "Mohammed"
                    lastName = "Iqbaal"
                    personUid = 1

                }
            },
        ),
    )

    MdcTheme{
        DiscussionPostDetailFragmentScreen(uiState)
    }
}