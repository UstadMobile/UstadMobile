package com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.CourseBlock
import java.util.*
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


@Composable
@Preview
fun CourseDiscussionDetailScreenPreview(){

    val currentMoment: Instant = Clock.System.now()

    CourseDiscussionDetailScreen(
        uiState = CourseDiscussionDetailUiState(

            courseBlock = CourseBlock().apply{
                cbTitle = "Discussions on Module 4: Statistics and Data Science"
                cbDescription = "Here Any discussion related to Module 4 of Data Science chapter goes here."
            },


//            posts = {
//                ListPagingSource(listOf(
//                    DiscussionPostWithDetails().apply {
//                        discussionPostTitle = "Can I join after week 2?"
//                        discussionPostUid = 0L
//                        discussionPostMessage = "Iam late to class, CAn I join after?"
//                        discussionPostVisible = true
//                        postRepliesCount = 4
//                        postLatestMessage = "Just make sure you submit a late assignment."
//                        authorPersonFirstNames = "Mike"
//                        authorPersonLastName = "Jones"
//                        discussionPostStartDate = currentMoment.epochSeconds
//                    },
//                    DiscussionPostWithDetails().apply {
//                        discussionPostTitle = "How to install xlib?"
//                        discussionPostMessage = "Which version of python do I need?"
//                        discussionPostVisible = true
//                        discussionPostUid = 1L
//                        postRepliesCount = 2
//                        postLatestMessage = "I have the same question"
//                        authorPersonFirstNames = "Bodium"
//                        authorPersonLastName = "Carafe"
//                        discussionPostStartDate = currentMoment.epochSeconds
//                    }
//
//                ))
//            }
        )
    )
}