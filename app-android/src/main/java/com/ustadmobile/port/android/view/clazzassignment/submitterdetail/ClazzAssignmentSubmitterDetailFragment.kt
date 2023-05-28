package com.ustadmobile.port.android.view.clazzassignment.submitterdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pending
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.ClazzAssignmentConstants.SUBMISSION_STATUS_ICON_MAP
import com.ustadmobile.port.android.view.clazzassignment.CommentListItem
import com.ustadmobile.port.android.view.clazzassignment.CourseAssignmentSubmissionListItem
import com.ustadmobile.port.android.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.port.android.view.composable.*


interface ClazzAssignmentDetailStudentProgressFragmentEventHandler {

    fun onSubmitGradeClicked()

    fun onSubmitGradeAndMarkNextClicked()

}

class ClazzAssignmentSubmitterDetailFragment: UstadBaseMvvmFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

        }
    }

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
                        text = messageIdMapResource(
                            map = ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID,
                            key = uiState.submissionStatus
                        ).capitalizeFirstLetter()
                    )
                },
                secondaryText = {
                    Text(stringResource(R.string.status))
                }
            )
        }

        item(key = "averagescore") {
            ListItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = ""
                    )
                },
                text = {
                    Text("${uiState.averageScore} ${stringResource(R.string.points)}")
                },
                secondaryText = {
                    Text(stringResource(R.string.score))
                }
            )
        }
        item(key = "submissionheader") {
            UstadDetailHeader {
                Text(stringResource(R.string.submissions))
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
                Text(stringResource(R.string.grades_scoring))
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
            items = uiState.marks,
            key = { Pair(CourseAssignmentMark.TABLE_ID, it.camUid) }
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
                    onClickSubmitGradeAndMarkNext = onClickSubmitGradeAndMarkNext
                )
            }
        }

        item(key = "private_comment_header") {
            UstadDetailHeader {
                Text(stringResource(R.string.private_comments))
            }
        }

        item(key = "new_private_comment") {
            UstadAddCommentListItem(
                text = stringResource(id = R.string.add_private_comment),
                enabled = uiState.fieldsEnabled,
                personUid = uiState.activeUserPersonUid,
                onClickAddComment =  onClickNewPrivateComment,
            )
        }

        items(
            items = uiState.privateCommentsList,
            key = { Pair(Comments.TABLE_ID, it.comment.commentsUid) }
        ) { comment ->
            CommentListItem(commentAndName = comment)
        }

        UstadListSpacerItem()
    }
}

@Composable
@Preview
fun ClazzAssignmentDetailStudentProgressScreenPreview(){

    val uiStateVal = ClazzAssignmentSubmitterDetailUiState(
        courseBlock = CourseBlock().apply {
            cbMaxPoints = 50
        },
        draftMark = CourseAssignmentMark().apply {

        },
        submissionList = listOf(
            CourseAssignmentSubmission().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casText = "I can haz cheezburger"
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            },
        ),
        marks = listOf(
            CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMark = 10f
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3f
                }
            }
        ),
        privateCommentsList = listOf(
            CommentsAndName(
                comment = Comments().apply {
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                },
                firstNames = "Bob",
                lastName = "Dylan"
            )
        ),
    )


    MdcTheme {
        ClazzAssignmentDetailStudentProgressScreen(uiStateVal)
    }
}