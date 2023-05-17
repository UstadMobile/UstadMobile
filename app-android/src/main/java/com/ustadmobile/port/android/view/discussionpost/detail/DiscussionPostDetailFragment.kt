package com.ustadmobile.port.android.view.discussionpost.detail


import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionPostWithPerson
import com.ustadmobile.lib.db.entities.Person
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.HtmlClickableTextField
import com.ustadmobile.port.android.view.composable.HtmlText
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import java.util.*


class DiscussionPostDetailFragment: UstadBaseMvvmFragment() {

    private val viewModel: DiscussionPostDetailViewModel by ustadViewModels{ di, savedStateHandle ->
        DiscussionPostDetailViewModel(di, savedStateHandle, requireDestinationViewName())
    }

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
    val uiState: DiscussionPostDetailUiState by viewModel.uiState.collectAsState(
        DiscussionPostDetailUiState()
    )

    val context = LocalContext.current

    DiscussionPostDetailFragmentScreen(
        uiState = uiState,
        onClickAddMessage = viewModel::addMessage,
        onClickDeleteMessage = viewModel::onClickDeleteEntry,
        onClickMessage = viewModel::onClickEntry,
        onClickEditReply = viewModel::onClickEditReply,
        onClickAddMessageD = viewModel::addMessageD,
    )


}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DiscussionPostDetailFragmentScreen(
    uiState: DiscussionPostDetailUiState = DiscussionPostDetailUiState(),
    onClickDeleteMessage: (DiscussionPostWithPerson) -> Unit = {},
    onClickMessage: (DiscussionPostWithPerson) -> Unit = {},
    onClickAddMessage: (String) -> Unit = {},
    onClickEditReply: () -> Unit = {},
    onClickAddMessageD: () -> Unit = {},
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


        HtmlText(
            html = uiState.discussionPost?.discussionPostTitle ?: "",
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
                    HtmlText(
                        html = uiState.discussionPost?.discussionPostMessage ?: "",
                        modifier = Modifier.padding(8.dp)
                    )


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
            modifier = Modifier.padding(4.dp)
        )


        val enabled = true
        var newReplyText = ""

        val onClickAddComment: (() -> Unit) = {

            newReplyText = uiState.messageReplyTitle?:""

            if(!newReplyText.isEmpty()) {
                onClickAddMessage(newReplyText)

            }else{
                onClickAddMessageD()
            }


        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ){

            HtmlClickableTextField(
                html = uiState.messageReplyTitle ?: "",
                label = stringResource(R.string.add_a_reply),
                onClick = onClickEditReply,
                modifier = Modifier.fillMaxWidth(fraction = 0.7F).testTag("add_a_reply")
            )

            Spacer(modifier = Modifier.width(16.dp))

            TextButton(
                onClick = onClickAddComment,
                modifier = Modifier,
                border = BorderStroke(0.dp, Color.Transparent),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.grey_a_40),
                )
            ) {
                Text(
                    text = stringResource(id = R.string.add),
                    textAlign = TextAlign.Start,
                    modifier = Modifier,
                    color = contentColorFor(
                        colorResource(id = R.color.grey_a_40)
                    )
                )
            }

        }


        Box(modifier = Modifier.weight(1f)) {

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = LazyListState(),
            ) {
                itemsIndexed(uiState.replies?: emptyList()) { _, reply ->

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        val context = LocalContext.current
                        val datePosted = remember { DateFormat.getDateFormat(context)
                            .format(Date(reply.discussionPostStartDate ?: 0)).toString() }

                        val fullName= reply.replyPerson?.fullName() ?: ""


                        if(reply.discussionPostStartedPersonUid == uiState.loggedInPersonUid){

                            UstadDetailField(
                                valueText = fullName,
                                labelText = reply.discussionPostMessage?: "",

                                icon = {
                                    //TODO: replace with avatar
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null
                                    )
                                },
                                secondaryActionContent = {
                                    IconButton(
                                        onClick = {
                                            onClickDeleteMessage(reply)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(id = R.string.delete),
                                        )
                                    }
                                }
                            )
                        }else{
                            UstadDetailField(
                                valueText = fullName,
                                labelText = reply.discussionPostMessage?: "",
                                icon = {
                                    //TODO: replace with avatar
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null
                                    )
                                },
                                secondaryActionContent = {

                                }
                            )
                        }

                    }

                }
            }
        }
    }

}


@Composable
@Preview
fun DiscussionPostDetailScreenFragmentPreview(){
    val uiState = DiscussionPostDetailUiState(
        discussionPost = DiscussionPostWithDetails().apply {
            discussionPostTitle = "Submitting an assignment"
            authorPersonFirstNames = "Mohammed"
            authorPersonLastName = "Iqbaal"
            discussionPostVisible = true
            discussionPostStartedPersonUid = 1
            discussionPostDiscussionTopicUid = 0
            discussionPostUid = 1
            discussionPostMessage = "Hi everyone, cna I get some help in how to submit the assignemnt?"
            discussionPostStartDate = systemTimeInMillis()

        },
        replies = listOf(
            DiscussionPostWithPerson().apply {

                discussionPostMessage = "I have the same question on Android"
                discussionPostDiscussionTopicUid = 1
                discussionPostStartedPersonUid = 2
                replyPerson = Person().apply {
                    firstNames = "Chahid"
                    lastName = "Dabir"
                    personUid = 2

                }
            },
            DiscussionPostWithPerson().apply {
                discussionPostMessage = "I think it is briefly explained in section 42"
                discussionPostDiscussionTopicUid = 1
                discussionPostStartedPersonUid = 3
                replyPerson = Person().apply {
                    firstNames = "Daanesh"
                    lastName = "Dabish"
                    personUid = 3

                }
            },

            DiscussionPostWithPerson().apply {
                discussionPostMessage = "Thanks everyone, I got it working now on Android!"
                discussionPostDiscussionTopicUid = 1
                discussionPostStartedPersonUid = 1
                replyPerson = Person().apply {
                    firstNames = "Mohammed"
                    lastName = "Iqbaal"
                    personUid = 1

                }
            },
        ),
        loggedInPersonUid = 1
    )

    MdcTheme{
        DiscussionPostDetailFragmentScreen(uiState)
    }
}