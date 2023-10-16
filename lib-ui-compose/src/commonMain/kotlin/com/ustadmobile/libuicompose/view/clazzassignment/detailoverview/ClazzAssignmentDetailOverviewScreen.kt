package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.HtmlClickableTextField
import com.ustadmobile.libuicompose.components.UstadAddCommentListItem
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzAssignmentDetailOverviewScreenForViewModel(viewModel: ClazzAssignmentDetailOverviewViewModel) {
    val uiState by viewModel.uiState.collectAsState(initial = ClazzAssignmentDetailOverviewUiState())

    //  TODO error
//    val localContext = LocalContext.current
    val newCourseCommentHint = stringResource(MR.strings.add_class_comment)
    val newPrivateCommentHint = stringResource(MR.strings.add_private_comment)

    ClazzAssignmentDetailOverviewScreen(
        uiState = uiState,
        onClickEditSubmission = viewModel::onClickEditSubmissionText,
        onClickNewPublicComment = {
            //  TODO error
//            CommentsBottomSheet(
//                hintText = newCourseCommentHint,
//                personUid = uiState.activeUserPersonUid,
//                onSubmitComment = {
//                    viewModel.onChangeCourseCommentText(it)
//                    viewModel.onClickSubmitCourseComment()
//                }
//            ).show(localContext.getContextSupportFragmentManager(), "public_comment_sheet")
        },
        onClickNewPrivateComment = {
            //  TODO error
//            CommentsBottomSheet(
//                hintText = newPrivateCommentHint,
//                personUid = uiState.activeUserPersonUid,
//                onSubmitComment = {
//                    viewModel.onChangePrivateCommentText(it)
//                    viewModel.onClickSubmitCourseComment()
//                }
//            ).show(localContext.getContextSupportFragmentManager(), "private_comment_sheet")
        },
        onClickSubmitSubmission = viewModel::onClickSubmit
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzAssignmentDetailOverviewScreen(
    uiState: ClazzAssignmentDetailOverviewUiState,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickNewPublicComment: () -> Unit = {},
    onClickNewPrivateComment: () -> Unit = {},
    onClickEditSubmission: () -> Unit = {},
    onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = {},
    onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit = { },
    onClickAddFileSubmission: () -> Unit = { },
    onClickSubmitSubmission: () -> Unit = { }
){

    //  TODO error
//    val privateCommentsPager = remember(uiState.privateComments) {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.privateComments
//        )
//    }
//    val privateCommentsLazyPagingItems = privateCommentsPager.flow.collectAsLazyPagingItems()

    //  TODO error
//    val courseCommentsPager = remember(uiState.courseComments) {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.courseComments
//        )
//    }
//    val courseCommentsLazyPagingItems = courseCommentsPager.flow.collectAsLazyPagingItems()

                              //  TODO error
//    val formattedDateTime = rememberFormattedDateTime(
//        timeInMillis = uiState.courseBlock?.cbDeadlineDate ?: 0,
//        timeZoneId = TimeZone.getDefault().id
//    )

    val policyMessageId = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS.firstOrNull {
        it.value == uiState.assignment?.caSubmissionPolicy
    }?.stringResource ?: MR.strings.submit_all_at_once_submission_policy


                     //  TODO error
//    val caFileType = stringIdMapResource(
//        map = SubmissionConstants.FILE_TYPE_MAP,
//        key = uiState.assignment?.caFileType ?: ClazzAssignment.FILE_TYPE_DOC
//    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            //  TODO error
//            .defaultScreenPadding()
    ) {
        if (uiState.caDescriptionVisible){
            item {
                //  TODO error
//                HtmlText(
//                    html = uiState.courseBlock?.cbDescription ?: "",
//                    modifier = Modifier.defaultItemPadding()
//                )
            }
        }

        if (uiState.cbDeadlineDateVisible){
            item {
//                UstadDetailField(
//                    valueText = "$formattedDateTime (${TimeZone.getDefault().id})",
//                    labelText = stringResource(MR.strings.deadline),
//                    //  TODO error
//                    imageId = R.drawable.ic_event_available_black_24dp,
//                    onClick = {  }
//                )
            }
        }

        item {
//            UstadDetailField(
//                valueText = stringResource(policyMessageId),
//                labelText = stringResource(MR.strings.submission_policy),
//                          //  TODO error
//                imageId = SUBMISSION_POLICY_MAP[uiState.assignment?.caSubmissionPolicy]
//                    ?: R.drawable.ic_baseline_task_alt_24,
//                onClick = {  }
//            )
        }

        item {
            //  TODO error
//            UstadAssignmentSubmissionHeader(
//                uiState = uiState.submissionHeaderUiState,
//            )
        }

        if(uiState.activeUserIsSubmitter) {
            item {
                UstadEditHeader(text = stringResource(MR.strings.your_submission))
            }

            item {
                if(uiState.activeUserCanSubmit) {
                    HtmlClickableTextField(
                        modifier = Modifier
                            .testTag("submission_text_field")
                            .fillMaxWidth(),
                        html = uiState.latestSubmission?.casText ?: "",
                        label = stringResource(MR.strings.text),
                        onClick = onClickEditSubmission
                    )
                }else {
                    //  TODO error
//                    HtmlText(
//                        modifier = Modifier
//                            .testTag("submission_text")
//                            .defaultItemPadding(),
//                        html = uiState.latestSubmission?.casText ?: "",
//                    )
                }
            }

            if(uiState.addFileVisible) {
                item {
                    ListItem(
                        modifier = Modifier.testTag("add_file"),
                        text = { Text(stringResource(MR.strings.add_file)) },
                        secondaryText = {
                            Text(
//                                "${stringResource(MR.strings.file_type_chosen)} $caFileType" +
                                        stringResource(MR.strings.max_number_of_files,
                                            uiState.assignment?.caNumberOfFiles ?: 0),
                            )
                        },
                        icon = {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    )
                }
            }

            items(
                items = uiState.latestSubmissionAttachments ?: emptyList(),
                key = { Pair(1, it.casaUid) }
            ){ attachment ->
                ListItem(
                    text = { Text(attachment.casaFileName ?: "") },
                    icon = {
                        Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null)
                    }
                )
            }
        }

        if (uiState.unassignedErrorVisible) {
            item {
                Text(uiState.unassignedError ?: "",)
                    //  TODO error
//                    modifier = Modifier.defaultItemPadding())
            }
        }

        if (uiState.submitSubmissionButtonVisible){
            item {
                Button(
                    onClick = onClickSubmitSubmission,
                    enabled = uiState.fieldsEnabled,
                    modifier = Modifier
                        .fillMaxWidth(),
                        //  TODO error
//                        .defaultItemPadding(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary
                    )
                ) {
                    Text(stringResource(MR.strings.submit).uppercase(),
                        color = contentColorFor(
                            MaterialTheme.colors.secondary
                        )
                    )
                }
            }
        }


        if(uiState.activeUserIsSubmitter) {
            item {
                ListItem(
                    text = { Text(stringResource(MR.strings.grades_class_age)) }
                )
            }

            item {
                UstadListFilterChipsHeader(
                    filterOptions = uiState.gradeFilterChips,
                    selectedChipId = uiState.selectedChipId,
                    enabled = uiState.fieldsEnabled,
                    onClickFilterChip = { onClickFilterChip(it) },
                )
            }

            items(
                items = uiState.markList,
                key = { Pair(3, it.courseAssignmentMark?.camUid ?: 0) }
            ){ mark ->
                //  TODO error
//                UstadCourseAssignmentMarkListItem(
//                    uiState = UstadCourseAssignmentMarkListItemUiState(
//                        mark = mark,
//                    ),
//                )
            }
        }

        item {
            ListItem(
                text = { Text(stringResource(MR.strings.class_comments)) }
            )
        }

        item {
            UstadAddCommentListItem(
                text = stringResource(MR.strings.add_class_comment),
                enabled = uiState.fieldsEnabled,
                personUid = 0,
                onClickAddComment = { onClickNewPublicComment() }
            )
        }

//        items(
//            items = courseCommentsLazyPagingItems,
//            key = { Pair(4, it.comment.commentsUid) }
//        ){ comment ->
//            //  TODO error
//            CommentListItem(commentAndName = comment)
//        }

        if(uiState.activeUserIsSubmitter) {
            item {
                ListItem(
                    text = { Text(stringResource(MR.strings.private_comments)) }
                )
            }

            item {
                UstadAddCommentListItem(
                    text = stringResource(MR.strings.add_private_comment),
                    enabled = uiState.fieldsEnabled,
                    personUid = 0,
                    onClickAddComment = { onClickNewPrivateComment() }
                )
            }

//            items(
//                items = privateCommentsLazyPagingItems,
//                key = { Pair(5, it.comment.commentsUid) }
//            ){ comment ->
//                //  TODO error
//                CommentListItem(commentAndName = comment)
//            }
        }

        //The collapse scrolling policy means we have to add space to ensure the user can scroll to
        // see last items - otherwise they could be hidden behind bottom navigation.
        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }


}
