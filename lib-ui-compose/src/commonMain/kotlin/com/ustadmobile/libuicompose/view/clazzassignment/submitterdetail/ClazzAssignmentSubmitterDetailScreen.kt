package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pending
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.port.android.view.clazzassignment.ClazzAssignmentConstants.SUBMISSION_STATUS_ICON_MAP
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadAddCommentListItem
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.view.clazzassignment.CourseAssignmentSubmissionListItem
import com.ustadmobile.libuicompose.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.libuicompose.util.compose.stringIdMapResource
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding

@Composable
fun ClazzAssignmentDetailStudentProgressScreenForViewModel(
    viewModel: ClazzAssignmentSubmitterDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzAssignmentSubmitterDetailUiState())

    val newCommentHintText = stringResource(MR.strings.add_private_comment)

//    val localContext = LocalContext.current

    ClazzAssignmentDetailStudentProgressScreen(
        uiState = uiState,
        onClickSubmitGrade = viewModel::onClickSubmitMark,
        onClickSubmitGradeAndMarkNext = viewModel::onClickSubmitMarkAndGoNext,
        onClickNewPrivateComment = {
            // TODO error
//            CommentsBottomSheet(
//                hintText = newCommentHintText,
//                personUid = uiState.activeUserPersonUid,
//                onSubmitComment = {
//                    viewModel.onChangePrivateComment(it)
//                    viewModel.onSubmitPrivateComment()
//                }
//            ).show(localContext.getContextSupportFragmentManager(), "private_comment_sheet")
        },
        onClickGradeFilterChip = viewModel::onClickGradeFilterChip,
        onClickOpenSubmission = viewModel::onClickSubmission,
        onChangeDraftMark = viewModel::onChangeDraftMark,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzAssignmentDetailStudentProgressScreen(
    uiState: ClazzAssignmentSubmitterDetailUiState,
    onClickSubmitGrade: () -> Unit = {},
    onClickSubmitGradeAndMarkNext: () -> Unit = {},
    onClickNewPrivateComment: () -> Unit = {},
    onClickGradeFilterChip: (MessageIdOption2) -> Unit = {},
    onClickOpenSubmission: (CourseAssignmentSubmission) -> Unit = {},
    onChangeDraftMark: (CourseAssignmentMark?) -> Unit = {},
){

    // TODO error
//    val privateCommentsPager = remember(uiState.privateCommentsList) {
//        Pager(
//            pagingSourceFactory = uiState.privateCommentsList,
//            config = PagingConfig(pageSize = 50, enablePlaceholders = true)
//        )
//    }
//
//    val privateCommentsLazyPagingItems = privateCommentsPager.flow.collectAsLazyPagingItems()


    LazyColumn (
        modifier = Modifier
            .defaultScreenPadding()
            .fillMaxSize()
    ) {
        item(key = "status") {
            ListItem(
                icon = {
                    Icon(
                        imageVector = SUBMISSION_STATUS_ICON_MAP[uiState.submissionStatus]
                            ?: Icons.Filled.Pending,
                        contentDescription = ""
                    )
                },
                text = {
                    Text(
                        text = stringIdMapResource(
                            map = ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID,
                            key = uiState.submissionStatus
                        ).capitalizeFirstLetter()
                    )
                },
                secondaryText = {
                    Text(stringResource(MR.strings.status))
                }
            )
        }

        if(uiState.scoreSummaryVisible) {
            item(key = "averagescore") {
                ListItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = ""
                        )
                    },
                    text = {
                        Text("${uiState.averageScore} ${stringResource(MR.strings.points)}")
                    },
                    secondaryText = {
                        Text(stringResource(MR.strings.score))
                    }
                )
            }
        }

        item(key = "submissionheader") {
            UstadDetailHeader {
                Text(stringResource(MR.strings.submissions))
            }
        }

        items(
            items = uiState.submissionList,
            key = { Pair(CourseAssignmentSubmission.TABLE_ID, it.casUid) }
        ) { submissionItem ->
            CourseAssignmentSubmissionListItem(
                submission = submissionItem,
                onClick = {
                    onClickOpenSubmission(submissionItem)
                }
            )
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
                )
            }
        }

        items(
            items = uiState.visibleMarks,
            key = { Pair(CourseAssignmentMark.TABLE_ID, it.courseAssignmentMark?.camUid ?: 0) }
        ) { mark ->
            UstadCourseAssignmentMarkListItem(
                uiState = uiState.markListItemUiState(mark)
            )
        }

        uiState.draftMark?.also { draftMarkVal ->
            item(key = "draftmark") {
                CourseAssignmentMarkEdit(
                    draftMark = draftMarkVal,
                    maxPoints = uiState.courseBlock?.cbMaxPoints?.toFloat() ?: 0f,
                    scoreError = uiState.submitMarkError,
                    onChangeDraftMark = onChangeDraftMark,
                    onClickSubmitGrade = onClickSubmitGrade,
                    submitGradeButtonMessageId = uiState.submitGradeButtonMessageId,
                    submitGradeButtonAndGoNextMessageId = uiState.submitGradeButtonAndGoNextMessageId,
                    onClickSubmitGradeAndMarkNext = onClickSubmitGradeAndMarkNext
                )
            }
        }

        item(key = "private_comment_header") {
            UstadDetailHeader {
                Text(stringResource(MR.strings.private_comments))
            }
        }

        item(key = "new_private_comment") {
            UstadAddCommentListItem(
                text = stringResource(MR.strings.add_private_comment),
                enabled = uiState.fieldsEnabled,
                personUid = uiState.activeUserPersonUid,
                onClickAddComment =  onClickNewPrivateComment,
            )
        }

//        items(
//            items = privateCommentsLazyPagingItems,
//            key = { Pair(Comments.TABLE_ID, it.comment.commentsUid) }
//        ) { comment ->
//            CommentListItem(commentAndName = comment)
//        }

        UstadListSpacerItem()
    }
}