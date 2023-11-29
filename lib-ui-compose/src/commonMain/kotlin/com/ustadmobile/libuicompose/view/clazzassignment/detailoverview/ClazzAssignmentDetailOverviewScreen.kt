package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.view.clazzassignment.CommentListItem

@Composable
fun ClazzAssignmentDetailOverviewScreen(viewModel: ClazzAssignmentDetailOverviewViewModel) {
    val uiState by viewModel.uiState.collectAsState(initial = ClazzAssignmentDetailOverviewUiState())

    ClazzAssignmentDetailOverviewScreen(
        uiState = uiState,
        onClickEditSubmission = viewModel::onClickEditSubmissionText,
        onChangeCourseComment = viewModel::onChangeCourseCommentText,
        onChangePrivateComment = viewModel::onChangePrivateCommentText,
        onClickSubmitCourseComment = viewModel::onClickSubmitCourseComment,
        onClickSubmitPrivateComment = viewModel::onClickSubmitPrivateComment,
        onClickSubmitSubmission = viewModel::onClickSubmit
    )
}

@Composable
fun ClazzAssignmentDetailOverviewScreen(
    uiState: ClazzAssignmentDetailOverviewUiState,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onChangeCourseComment: (String) -> Unit = {},
    onChangePrivateComment: (String) -> Unit = {},
    onClickSubmitCourseComment: () -> Unit = {},
    onClickSubmitPrivateComment: () -> Unit = {},
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
            item(key = "cbDescription") {
                UstadHtmlText(
                    html = uiState.courseBlock?.cbDescription ?: "",
                    modifier = Modifier.defaultItemPadding()
                )
            }
        }

        if (uiState.cbDeadlineDateVisible){
            item(key = "deadline") {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = {
                        Icon(
                            Icons.Filled.EventAvailable,
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text("$formattedDateTime (${TimeZone.getDefault().id})")},
                    supportingContent = { Text(stringResource(MR.strings.deadline)) }
                )
            }
        }

        item(key = "submissionpolicy") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                leadingContent = {
                    Icon(
                        SUBMISSION_POLICY_MAP[uiState.assignment?.caSubmissionPolicy]
                    ?: Icons.Default.TaskAlt,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(stringResource(policyMessageId))},
                supportingContent = { Text(stringResource(MR.strings.submission_policy)) }
            )
        }

        uiState.courseGroupSet?.also { groupSet ->
            item(key = "submissionGroups") {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Default.Groups, contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text(groupSet.cgsName ?: "")
                    },
                    supportingContent = {
                        Text(stringResource(MR.strings.group_submission))
                    }
                )
            }
        }

        item {
            UstadAssignmentSubmissionHeader(
                uiState = uiState.submissionHeaderUiState,
            )
        }

        if(uiState.unassignedErrorVisible) {
            item("unassigned_error") {
                Text(
                    text = uiState.unassignedError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.defaultItemPadding()
                )
            }
        }

        if(uiState.activeUserIsSubmitter) {
            item(key = "your_submission_header") {
                val suffix = if (uiState.isGroupSubmission) {
                    stringResource(MR.strings.group_number, uiState.submitterUid.toString())
                } else {
                    ""
                }
                UstadEditHeader(stringResource(MR.strings.your_submission) + " " + suffix)
            }


            if(uiState.submissionTextFieldVisible) {
                item(key = "submission") {
                    if(uiState.activeUserCanSubmit) {
                        UstadRichTextEdit(
                            modifier = Modifier
                                .testTag("submission_text_field")
                                .fillMaxWidth(),
                            html = uiState.latestSubmission?.casText ?: "",
                            editInNewScreenLabel = stringResource(MR.strings.text),
                            placeholder = { Text(stringResource(MR.strings.text)) },
                            onHtmlChange = {
                                uiState.latestSubmission?.also {  latestSubmission ->
                                    latestSubmission.shallowCopy {
                                        casText = it
                                    }
                                }
                            },
                            onClickToEditInNewScreen = onClickEditSubmission
                        )
                    }else {
                        UstadHtmlText(
                            modifier = Modifier
                                .testTag("submission_text")
                                .defaultItemPadding(),
                            html = uiState.latestSubmission?.casText ?: ""
                        )
                    }
                }

            }

            if(uiState.addFileVisible) {
                item(key = "add_file_button") {
                    ListItem(
                        modifier = Modifier.testTag("add_file"),
                        headlineContent = { Text(stringResource(MR.strings.add_file)) },
                        supportingContent = {
                            Text(
                                "${stringResource(MR.strings.file_type_chosen)} $caFileType" +
                                        stringResource(MR.strings.max_number_of_files,
                                            uiState.assignment?.caNumberOfFiles ?: 0),
                            )
                        },
                        leadingContent = {
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
                    headlineContent = { Text(attachment.casaFileName ?: "") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null)
                    }
                )
            }

            if (uiState.submitSubmissionButtonVisible){
                item(key = "submit_button") {
                    Button(
                        onClick = onClickSubmitSubmission,
                        enabled = uiState.fieldsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultItemPadding(),
                    ) {
                        Text(stringResource(MR.strings.submit))
                    }
                }
            }

            uiState.submissionError?.also { submissionError ->
                item(key = "submission_error") {
                    Text(
                        text = submissionError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.defaultItemPadding()
                    )
                }
            }


            item(key = "grades_header") {
                ListItem(
                    headlineContent = { Text(stringResource(MR.strings.grades_scoring)) }
                )
            }

            item(key = "grades_filter_chips") {
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

        } //End section that is only for submitters

        item {
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.class_comments)) }
            )
        }


        if(uiState.showClassComments) {
            item(key = "add_class_comment_item") {
                UstadAddCommentListItem(
                    modifier = Modifier.testTag("add_class_comment"),
                    commentText = uiState.newCourseCommentText,
                    commentLabel = stringResource(MR.strings.add_class_comment),
                    enabled = uiState.fieldsEnabled,
                    currentUserPersonUid = uiState.activeUserPersonUid,
                    onSubmitComment = onClickSubmitCourseComment,
                    onCommentChanged = onChangeCourseComment
                )
            }

            ustadPagedItems(
                pagingItems = courseCommentsLazyPagingItems,
                key = { Pair(4, it.comment.commentsUid) }
            ){
                CommentListItem(commentAndName = it)
            }
        }


        if(uiState.showPrivateComments) {
            item(key = "add_private_comment_item") {
                ListItem(
                    headlineContent = { Text(stringResource(MR.strings.private_comments)) }
                )
            }

            item {
                UstadAddCommentListItem(
                    modifier = Modifier.testTag("add_private_comment"),
                    commentText = uiState.newPrivateCommentText,
                    commentLabel = stringResource(MR.strings.add_private_comment),
                    enabled = uiState.fieldsEnabled,
                    currentUserPersonUid = uiState.activeUserPersonUid,
                    onSubmitComment = onClickSubmitPrivateComment,
                    onCommentChanged = onChangePrivateComment
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
