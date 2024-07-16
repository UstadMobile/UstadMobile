package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.libuicompose.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.SUBMISSION_POLICY_MAP
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazzassignment.averageMark
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailoverviewSubmissionUiState
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.libuicompose.components.PickFileOptions
import com.ustadmobile.libuicompose.components.PickType
import com.ustadmobile.libuicompose.components.UstadCourseBlockHeader
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadOpeningBlobInfoBottomSheet
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
import com.ustadmobile.libuicompose.components.isDesktop
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import com.ustadmobile.libuicompose.view.clazzassignment.CommentListItem
import com.ustadmobile.libuicompose.view.clazzassignment.CourseAssignmentSubmissionComponent
import com.ustadmobile.libuicompose.view.clazzassignment.CourseAssignmentSubmissionFileListItem
import com.ustadmobile.libuicompose.view.clazzassignment.UstadAssignmentSubmissionStatusHeaderItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ClazzAssignmentDetailOverviewScreen(viewModel: ClazzAssignmentDetailOverviewViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzAssignmentDetailOverviewUiState(), Dispatchers.Main.immediate
    )

    val filePickLauncher = rememberUstadFilePickLauncher {
        viewModel.onAddSubmissionFile(
            uri = it.uri,
            fileName =  it.fileName,
            mimeType = it.mimeType ?: "application/octet-stream",
            size = it.size
        )
    }

    uiState.openingFileSubmissionState?.also { openingState ->
        UstadOpeningBlobInfoBottomSheet(
            openingBlobState = openingState,
            onDismissRequest = viewModel::onDismissOpenFileSubmission,
        )
    }

    ClazzAssignmentDetailOverviewScreen(
        uiState = uiState,
        newCourseCommentFlow = viewModel.newCourseCommentText,
        newPrivateCommentFlow = viewModel.newPrivateCommentText,
        editableSubmissionFlow = viewModel.editableSubmissionUiState,
        onClickEditSubmission = viewModel::onClickEditSubmissionText,
        onChangeCourseComment = viewModel::onChangeCourseCommentText,
        onChangePrivateComment = viewModel::onChangePrivateCommentText,
        onClickSubmitCourseComment = viewModel::onClickSubmitCourseComment,
        onClickSubmitPrivateComment = viewModel::onClickSubmitPrivateComment,
        onClickSubmitSubmission = viewModel::onClickSubmit,
        onChangeSubmissionText = viewModel::onChangeSubmissionText,
        onClickCourseGroupSet = viewModel::onClickCourseGroupSet,
        onClickMarksFilterChip = viewModel::onClickMarksFilterChip,
        onClickAddFileSubmission = {
            filePickLauncher(PickFileOptions(pickType = PickType.FILE))
        },
        onRemoveSubmissionFile = viewModel::onRemoveSubmissionFile,
        onOpenSubmissionFile =  viewModel::onOpenSubmissionFile,
        onSendSubmissionFile = if(!isDesktop()) viewModel::onSendSubmissionFile else null,
        onToggleSubmissionExpandCollapse = viewModel::onToggleSubmissionExpandCollapse,
        onDeleteComment = viewModel::onDeleteComment,
    )
}

@Composable
fun ClazzAssignmentDetailOverviewScreen(
    uiState: ClazzAssignmentDetailOverviewUiState,
    editableSubmissionFlow: Flow<ClazzAssignmentDetailoverviewSubmissionUiState>,
    newPrivateCommentFlow: Flow<String>,
    newCourseCommentFlow: Flow<String>,
    onClickMarksFilterChip: (MessageIdOption2) -> Unit = {},
    onChangeCourseComment: (String) -> Unit = {},
    onChangePrivateComment: (String) -> Unit = {},
    onClickSubmitCourseComment: () -> Unit = {},
    onClickSubmitPrivateComment: () -> Unit = {},
    onClickEditSubmission: () -> Unit = {},
    onChangeSubmissionText: (String) -> Unit = { },
    onClickAddFileSubmission: () -> Unit = { },
    onClickSubmitSubmission: () -> Unit = { },
    onClickCourseGroupSet: () -> Unit = { },
    onRemoveSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit = { },
    onOpenSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit = { },
    onSendSubmissionFile: ((CourseAssignmentSubmissionFileAndTransferJob) -> Unit)? = null,
    onToggleSubmissionExpandCollapse: (CourseAssignmentSubmission) -> Unit = { },
    onDeleteComment: (Comments) -> Unit = { },
){
    val refreshCommandFlow = rememberEmptyFlow<RefreshCommand>()

    val privateCommentsRepoResult = rememberDoorRepositoryPager(
        uiState.privateComments, refreshCommandFlow
    )
    val privateCommentsLazyPagingItems = privateCommentsRepoResult.lazyPagingItems

    val courseCommentsRepoResult = rememberDoorRepositoryPager(
        uiState.courseComments, refreshCommandFlow
    )
    val courseCommentsLazyPagingItems = courseCommentsRepoResult.lazyPagingItems

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

    val linkExtractor = rememberLinkExtractor()

    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    val timeFormatter = rememberTimeFormatter()

    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(key = "header") {
            UstadCourseBlockHeader(
                block = uiState.courseBlock,
                picture = uiState.courseBlockPicture,
                modifier = Modifier.defaultItemPadding(top = 16.dp).fillMaxWidth()
            )
        }

        item(key = "cbDescription") {
            if (uiState.caDescriptionVisible){
                UstadHtmlText(
                    html = uiState.courseBlock?.cbDescription ?: "",
                    modifier = Modifier.defaultItemPadding(top = 16.dp)
                )
            }
        }

        item(key = "deadline") {
            if (uiState.cbDeadlineDateVisible){
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.EventAvailable,
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(formattedDateTime)},
                    supportingContent = { Text(stringResource(MR.strings.deadline)) }
                )
            }
        }

        item(key = "submissionpolicy") {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.clickable(onClick = onClickCourseGroupSet),
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

        UstadAssignmentSubmissionStatusHeaderItems(
            submissionStatus = uiState.submissionStatus,
            averageMark = uiState.markList.averageMark(),
            maxPoints = uiState.courseBlock?.cbMaxPoints ?: 0f,
            submissionPenaltyPercent = uiState.courseBlock?.cbLateSubmissionPenalty ?: 0,
        )

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
            if(uiState.activeUserCanSubmit) {
                item(key = "your_submission_header") {
                    val suffix = if (uiState.isGroupSubmission) {
                        "(${stringResource(MR.strings.group_number, uiState.submitterUid.toString())})"
                    } else {
                        ""
                    }
                    UstadEditHeader(stringResource(MR.strings.your_submission) + " " + suffix)
                }
            }

            if(uiState.submissionTextFieldVisible) {
                item(key = "submission") {
                    CourseAssignmentSubmissionEdit(
                        stateFlow = editableSubmissionFlow,
                        onChangeSubmissionText = onChangeSubmissionText,
                        onClickEditSubmission = onClickEditSubmission,
                    )
                }
            }

            if(uiState.addFileSubmissionVisible) {
                item(key = "add_file_button") {
                    ListItem(
                        modifier = Modifier.testTag("add_file").clickable {
                            onClickAddFileSubmission()
                        },
                        headlineContent = { Text(stringResource(MR.strings.add_file)) },
                        supportingContent = {
                            Text(
                            "${stringResource(MR.strings.file_type_chosen)} $caFileType" +
                                "${stringResource(MR.strings.number_of_files)} ${uiState.assignment?.caNumberOfFiles ?: 0}" +
                                "${stringResource(MR.strings.size_limit)}: ${uiState.assignment?.caSizeLimit}"
                            )
                        },
                        leadingContent = {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    )
                }
            }

            items(
                items = uiState.editableSubmissionFiles,
                key = { Pair(CourseAssignmentSubmissionFile.TABLE_ID, it.submissionFile?.casaUid) }
            ) { item ->
                CourseAssignmentSubmissionFileListItem(
                    fileAndTransferJob = item,
                    onRemove = onRemoveSubmissionFile,
                    onClickOpen = onOpenSubmissionFile,
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

            uiState.submissions.forEachIndexed { index, submissionAndFiles ->
                val isCollapsedVal = submissionAndFiles.submission.casUid in uiState.collapsedSubmissions
                item(key = "submission_${submissionAndFiles.submission.casUid}") {
                    CourseAssignmentSubmissionComponent(
                        submission = submissionAndFiles.submission,
                        submissionNum = uiState.submissions.size - index,
                        isCollapsed = isCollapsedVal,
                        onToggleCollapse = {
                            onToggleSubmissionExpandCollapse(submissionAndFiles.submission)
                        }
                    )
                }

                if(!isCollapsedVal) {
                    items(
                        items = submissionAndFiles.files,
                        key = { Pair("submittedfile", it.submissionFile?.casaUid ?: 0)}
                    ) { file ->
                        CourseAssignmentSubmissionFileListItem(
                            fileAndTransferJob = file,
                            onClickOpen = onOpenSubmissionFile,
                            onSend = onSendSubmissionFile,
                        )
                    }
                }
            }

            item(key = "grades_header") {
                ListItem(
                    headlineContent = { Text(stringResource(MR.strings.grades_scoring)) }
                )
            }

            if(uiState.gradeFilterChipsVisible) {
                item(key = "grades_filter_chips") {
                    UstadListFilterChipsHeader(
                        filterOptions = uiState.gradeFilterChips,
                        selectedChipId = uiState.selectedChipId,
                        enabled = uiState.fieldsEnabled,
                        onClickFilterChip = onClickMarksFilterChip,
                    )
                }
            }

            items(
                items = uiState.visibleMarks,
                key = { Pair(3, it.courseAssignmentMark?.camUid ?: 0) }
            ){ mark ->
                UstadCourseAssignmentMarkListItem(
                    uiState = UstadCourseAssignmentMarkListItemUiState(
                        mark = mark,
                        localDateTimeNow = uiState.localDateTimeNow,
                        dayOfWeekStrings = uiState.dayOfWeekStringMap,
                    ),
                    timeFormatter = timeFormatter,
                    dateFormat = dateFormatter,
                )
            }

        } //End section that is only for submitters

        item("class_comments_header") {
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.class_comments)) }
            )
        }


        if(uiState.showClassComments) {
            item(key = "add_class_comment_item") {
                UstadAddCommentListItem(
                    modifier = Modifier.testTag("add_class_comment"),
                    commentText = newCourseCommentFlow,
                    commentLabel = stringResource(MR.strings.add_class_comment),
                    enabled = uiState.fieldsEnabled,
                    currentUserPersonUid = uiState.activeUserPersonUid,
                    currentUserPersonName = uiState.activeUserPersonName,
                    currentUserPictureUri = uiState.activeUserPictureUri,
                    onSubmitComment = onClickSubmitCourseComment,
                    onCommentChanged = onChangeCourseComment,
                )
            }

            ustadPagedItems(
                pagingItems = courseCommentsLazyPagingItems,
                key = { Pair(4, it.comment.commentsUid) }
            ){
                CommentListItem(
                    commentAndName = it,
                    linkExtractor = linkExtractor,
                    localDateTimeNow = uiState.localDateTimeNow,
                    timeFormatter = timeFormatter,
                    dateFormatter = dateFormatter,
                    dayOfWeekStringMap = uiState.dayOfWeekStringMap,
                    showModerateOptions = uiState.showModerateOptions,
                    onDeleteComment = onDeleteComment,
                )
            }
        }


        if(uiState.showPrivateComments) {
            item(key = "add_private_comment_header") {
                ListItem(
                    headlineContent = { Text(stringResource(MR.strings.private_comments)) }
                )
            }

            item(key = "add_private_comment_item") {
                UstadAddCommentListItem(
                    modifier = Modifier.testTag("add_private_comment"),
                    commentText = newPrivateCommentFlow,
                    commentLabel = stringResource(MR.strings.add_private_comment),
                    enabled = uiState.fieldsEnabled,
                    currentUserPersonUid = uiState.activeUserPersonUid,
                    currentUserPersonName = uiState.activeUserPersonName,
                    currentUserPictureUri = uiState.activeUserPictureUri,
                    onSubmitComment = onClickSubmitPrivateComment,
                    onCommentChanged = onChangePrivateComment,
                )
            }

            ustadPagedItems(
                pagingItems = privateCommentsLazyPagingItems,
                key = { Pair(5, it.comment.commentsUid) }
            ){ comment ->
                CommentListItem(
                    commentAndName = comment,
                    linkExtractor = linkExtractor,
                    localDateTimeNow = uiState.localDateTimeNow,
                    timeFormatter = timeFormatter,
                    dateFormatter = dateFormatter,
                    dayOfWeekStringMap = uiState.dayOfWeekStringMap,
                    showModerateOptions = uiState.showModerateOptions,
                    onDeleteComment = onDeleteComment,
                )
            }
        }
    }


}
