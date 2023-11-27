package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.TaskAlt
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
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadAddCommentListItem
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.libuicompose.view.clazzassignment.UstadAssignmentSubmissionHeader
import com.ustadmobile.libuicompose.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.SUBMISSION_POLICY_MAP
import dev.icerock.moko.resources.compose.stringResource
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.libuicompose.view.clazzassignment.CommentListItem

@Composable
fun ClazzAssignmentDetailOverviewScreenForViewModel(viewModel: ClazzAssignmentDetailOverviewViewModel) {
    val uiState by viewModel.uiState.collectAsState(initial = ClazzAssignmentDetailOverviewUiState())

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

    val privateCommentsPager = remember(uiState.privateComments) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.privateComments
        )
    }
    val privateCommentsLazyPagingItems = privateCommentsPager.flow.collectAsLazyPagingItems()

    val courseCommentsPager = remember(uiState.courseComments) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.courseComments
        )
    }
    val courseCommentsLazyPagingItems = courseCommentsPager.flow.collectAsLazyPagingItems()

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = uiState.courseBlock?.cbDeadlineDate ?: 0,
        timeZoneId = TimeZone.getDefault().id
    )

    val policyMessageId = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS.firstOrNull {
        it.value == uiState.assignment?.caSubmissionPolicy
    }?.stringResource ?: MR.strings.submit_all_at_once_submission_policy


    val caFileType = stringIdMapResource(
        map = SubmissionConstants.FILE_TYPE_MAP,
        key = uiState.assignment?.caFileType ?: ClazzAssignment.FILE_TYPE_DOC
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        if (uiState.caDescriptionVisible){
            item {
                UstadHtmlText(
                    html = uiState.courseBlock?.cbDescription ?: "",
                    modifier = Modifier.defaultItemPadding()
                )
            }
        }

        if (uiState.cbDeadlineDateVisible){
            item {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    icon = {
                        Icon(
                            Icons.Filled.EventAvailable,
                            contentDescription = null
                        )
                    },
                    text = { Text("$formattedDateTime (${TimeZone.getDefault().id})")},
                    secondaryText = { Text(stringResource(MR.strings.deadline)) }
                )
            }
        }

        item {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(),
                icon = {
                    Icon(
                        SUBMISSION_POLICY_MAP[uiState.assignment?.caSubmissionPolicy]
                    ?: Icons.Default.TaskAlt,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(policyMessageId))},
                secondaryText = { Text(stringResource(MR.strings.submission_policy)) }
            )
        }

        item {
            UstadAssignmentSubmissionHeader(
                uiState = uiState.submissionHeaderUiState,
            )
        }

        if(uiState.activeUserIsSubmitter) {
            item {
                UstadEditHeader(text = stringResource(MR.strings.your_submission))
            }

            item {
                if(uiState.activeUserCanSubmit) {
//                    HtmlClickableTextField(
//                        modifier = Modifier
//                            .testTag("submission_text_field")
//                            .fillMaxWidth(),
//                        html = uiState.latestSubmission?.casText ?: "",
//                        label = stringResource(MR.strings.text),
//                        onClick = onClickEditSubmission
//                    )
                }else {
                    UstadHtmlText(
                        modifier = Modifier
                            .testTag("submission_text")
                            .defaultItemPadding(),
                        html = uiState.latestSubmission?.casText ?: "",
                    )
                }
            }

            if(uiState.addFileVisible) {
                item {
                    ListItem(
                        modifier = Modifier.testTag("add_file"),
                        text = { Text(stringResource(MR.strings.add_file)) },
                        secondaryText = {
                            Text(
                                "${stringResource(MR.strings.file_type_chosen)} $caFileType" +
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
                Text(uiState.unassignedError ?: "",
                    modifier = Modifier.defaultItemPadding())
            }
        }

        if (uiState.submitSubmissionButtonVisible){
            item {
                Button(
                    onClick = onClickSubmitSubmission,
                    enabled = uiState.fieldsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
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
                UstadCourseAssignmentMarkListItem(
                    uiState = UstadCourseAssignmentMarkListItemUiState(
                        mark = mark,
                    ),
                )
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

        ustadPagedItems(
            pagingItems = courseCommentsLazyPagingItems,
            key = { Pair(4, it.comment.commentsUid) }
        ){
            CommentListItem(commentAndName = it)
        }

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

            ustadPagedItems(
                pagingItems = privateCommentsLazyPagingItems,
                key = { Pair(5, it.comment.commentsUid) }
            ){ comment ->
                CommentListItem(commentAndName = comment)
            }
        }

        //The collapse scrolling policy means we have to add space to ensure the user can scroll to
        // see last items - otherwise they could be hidden behind bottom navigation.
        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }


}
