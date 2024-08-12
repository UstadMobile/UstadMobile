package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.averageMark
import com.ustadmobile.core.viewmodel.clazzassignment.submissionStatusFor
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.components.UstadAddCommentListItem
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.components.UstadOpeningBlobInfoBottomSheet
import com.ustadmobile.libuicompose.components.isDesktop
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import com.ustadmobile.libuicompose.view.clazzassignment.CommentListItem
import com.ustadmobile.libuicompose.view.clazzassignment.CourseAssignmentSubmissionComponent
import com.ustadmobile.libuicompose.view.clazzassignment.CourseAssignmentSubmissionFileListItem
import com.ustadmobile.libuicompose.view.clazzassignment.UstadAssignmentSubmissionStatusHeaderItems
import com.ustadmobile.libuicompose.view.clazzassignment.UstadCourseAssignmentMarkListItem
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.util.TimeZone

@Composable
fun ClazzAssignmentSubmitterDetailScreen(
    viewModel: ClazzAssignmentSubmitterDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzAssignmentSubmitterDetailUiState(), Dispatchers.Main.immediate
    )

    ClazzAssignmentSubmitterDetailScreen(
        uiState = uiState,
        newPrivateCommentText = viewModel.newPrivateCommentText,
        onClickSubmitGrade = viewModel::onClickSubmitMark,
        onClickSubmitGradeAndMarkNext = viewModel::onClickSubmitMarkAndGoNext,
        onChangePrivateComment = viewModel::onChangePrivateComment,
        onClickSubmitPrivateComment = viewModel::onSubmitPrivateComment,
        onClickGradeFilterChip = viewModel::onClickGradeFilterChip,
        onChangeDraftMark = viewModel::onChangeDraftMark,
        onToggleSubmissionExpandCollapse = viewModel::onToggleSubmissionExpandCollapse,
        onOpenSubmissionFile = viewModel::onOpenSubmissionFile,
        onSendSubmissionFile = if(!isDesktop()) viewModel::onSendSubmissionFile else null,
        onDeleteComment = viewModel::onDeleteComment,
    )

    uiState.openingFileState?.also { openingState ->
        UstadOpeningBlobInfoBottomSheet(
            openingBlobState = openingState,
            onDismissRequest = viewModel::onDismissOpenFileSubmission,
        )
    }
}

@Composable
fun ClazzAssignmentSubmitterDetailScreen(
    uiState: ClazzAssignmentSubmitterDetailUiState,
    newPrivateCommentText: Flow<String>,
    onClickSubmitGrade: () -> Unit = {},
    onClickSubmitGradeAndMarkNext: () -> Unit = {},
    onChangePrivateComment: (String) -> Unit = {},
    onClickSubmitPrivateComment: () -> Unit = {},
    onClickGradeFilterChip: (MessageIdOption2) -> Unit = {},
    onChangeDraftMark: (CourseAssignmentMark?) -> Unit = {},
    onToggleSubmissionExpandCollapse: (CourseAssignmentSubmission) -> Unit = { },
    onOpenSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit = { },
    onSendSubmissionFile: ((CourseAssignmentSubmissionFileAndTransferJob) -> Unit)? = null,
    onDeleteComment: (Comments) -> Unit = { },
){

    val privateCommentsPager = rememberDoorRepositoryPager(
        uiState.privateCommentsList, rememberEmptyFlow()
    )

    val privateCommentsLazyPagingItems = privateCommentsPager.lazyPagingItems

    val linkExtractor = rememberLinkExtractor()

    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    UstadLazyColumn (
        modifier = Modifier
            .defaultScreenPadding()
            .fillMaxSize()
    ) {
        UstadAssignmentSubmissionStatusHeaderItems(
            submissionStatus = submissionStatusFor(uiState.marks, uiState.submissionList),
            averageMark = uiState.marks.averageMark(),
            maxPoints = uiState.block?.courseBlock?.cbMaxPoints ?: 0f,
            submissionPenaltyPercent = uiState.block?.courseBlock?.cbLateSubmissionPenalty ?: 0
        )

        item(key = "submissionheader") {
            UstadDetailHeader {
                Text(stringResource(MR.strings.submissions))
            }
        }

        uiState.submissionList.forEachIndexed { index, submissionAndFiles ->
            val isCollapsedVal = submissionAndFiles.submission.casUid in uiState.collapsedSubmissions
            item(key = Pair(CourseAssignmentSubmission.TABLE_ID, submissionAndFiles.submission.casUid)) {
                CourseAssignmentSubmissionComponent(
                    submission = submissionAndFiles.submission,
                    submissionNum = uiState.submissionList.size - index,
                    isCollapsed = isCollapsedVal,
                    onToggleCollapse = {
                        onToggleSubmissionExpandCollapse(submissionAndFiles.submission)
                    }
                )
            }

            if(!isCollapsedVal) {
                items(
                    items = submissionAndFiles.files,
                    key = { Pair(CourseAssignmentSubmission.TABLE_ID, it.submissionFile?.casaUid ?: 0) }
                ) {
                    CourseAssignmentSubmissionFileListItem(
                        fileAndTransferJob = it,
                        onClickOpen = onOpenSubmissionFile,
                        onSend = onSendSubmissionFile,
                    )
                }
            }
        }

        item(key = "gradesheader") {
            UstadDetailHeader {
                Text(stringResource(MR.strings.grades_scoring))
            }
        }

        if(uiState.markListFilterChipsVisible) {
            item(key = "gradefilterchips") {
                UstadListFilterChipsHeader(
                    filterOptions = uiState.markListFilterOptions,
                    selectedChipId = uiState.markListSelectedChipId,
                    onClickFilterChip = onClickGradeFilterChip,
                    enabled = uiState.fieldsEnabled,
                )
            }
        }

        items(
            items = uiState.visibleMarks,
            key = { Pair(CourseAssignmentMark.TABLE_ID, it.courseAssignmentMark?.camUid ?: 0) }
        ) { mark ->
            UstadCourseAssignmentMarkListItem(
                uiState = uiState.markListItemUiState(mark),
                timeFormatter = timeFormatter,
                dateFormat = dateFormatter,
            )
        }

        uiState.draftMark?.also { draftMarkVal ->
            item(key = "draftmark") {
                CourseAssignmentMarkEdit(
                    draftMark = draftMarkVal,
                    maxPoints = uiState.block?.courseBlock?.cbMaxPoints ?: 0f,
                    scoreError = uiState.submitMarkError,
                    onChangeDraftMark = onChangeDraftMark,
                    onClickSubmitGrade = onClickSubmitGrade,
                    submitGradeButtonMessageId = uiState.submitGradeButtonMessageId,
                    submitGradeButtonAndGoNextMessageId = uiState.submitGradeButtonAndGoNextMessageId,
                    onClickSubmitGradeAndMarkNext = onClickSubmitGradeAndMarkNext
                )
            }
        }

        if(uiState.newPrivateCommentTextVisible) {
            item(key = "private_comment_header") {
                UstadDetailHeader {
                    Text(stringResource(MR.strings.private_comments))
                }
            }

            item(key = "new_private_comment") {
                UstadAddCommentListItem(
                    modifier = Modifier.testTag("add_private_comment"),
                    commentText = newPrivateCommentText,
                    commentLabel = stringResource(MR.strings.add_private_comment),
                    enabled = uiState.fieldsEnabled,
                    currentUserPersonUid = uiState.activeUserPersonUid,
                    onSubmitComment =  onClickSubmitPrivateComment,
                    currentUserPersonName = uiState.activeUserPersonName,
                    currentUserPictureUri = uiState.activeUserPictureUri,
                    onCommentChanged = onChangePrivateComment
                )
            }
        }

        ustadPagedItems(
            pagingItems = privateCommentsLazyPagingItems,
            key = { Pair(Comments.TABLE_ID, it.comment.commentsUid) }
        ) { comment ->
            CommentListItem(
                commentAndName = comment,
                linkExtractor = linkExtractor,
                localDateTimeNow = uiState.localDateTimeNow,
                timeFormatter = timeFormatter,
                dateFormatter = dateFormatter,
                dayOfWeekStringMap = uiState.dayOfWeekStrings,
                showModerateOptions = uiState.showModerateOptions,
                onDeleteComment = onDeleteComment,
            )
        }

        UstadListSpacerItem()
    }
}