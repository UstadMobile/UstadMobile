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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.ClazzAssignmentConstants.SUBMISSION_STATUS_ICON_MAP
import com.ustadmobile.port.android.view.clazzassignment.CommentListItem
import com.ustadmobile.port.android.view.clazzassignment.CourseAssignmentSubmissionListItem
import com.ustadmobile.port.android.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.port.android.util.compose.stringIdMapResource
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.clazzassignment.CommentsBottomSheet
import com.ustadmobile.core.R as CR

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
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

                }
            }
        }
    }

}

@Composable
fun ClazzAssignmentDetailStudentProgressScreen(
    viewModel: ClazzAssignmentSubmitterDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzAssignmentSubmitterDetailUiState())

    val newCommentHintText = stringResource(CR.string.add_private_comment)

    val localContext = LocalContext.current

    ClazzAssignmentDetailStudentProgressScreen(
        uiState = uiState,
        onClickSubmitGrade = viewModel::onClickSubmitMark,
        onClickSubmitGradeAndMarkNext = viewModel::onClickSubmitMarkAndGoNext,
        onClickNewPrivateComment = {
            CommentsBottomSheet(
                hintText = newCommentHintText,
                personUid = uiState.activeUserPersonUid,
                onSubmitComment = {
                    viewModel.onChangePrivateComment(it)
                    viewModel.onSubmitPrivateComment()
                }
            ).show(localContext.getContextSupportFragmentManager(), "private_comment_sheet")
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

    val privateCommentsPager = remember(uiState.privateCommentsList) {
        Pager(
            pagingSourceFactory = uiState.privateCommentsList,
            config = PagingConfig(pageSize = 50, enablePlaceholders = true)
        )
    }

    val privateCommentsLazyPagingItems = privateCommentsPager.flow.collectAsLazyPagingItems()


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
                    Text(stringResource(CR.string.status))
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
                        Text("${uiState.averageScore} ${stringResource(CR.string.points)}")
                    },
                    secondaryText = {
                        Text(stringResource(CR.string.score))
                    }
                )
            }
        }

        item(key = "submissionheader") {
            UstadDetailHeader {
                Text(stringResource(CR.string.submissions))
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
                Text(stringResource(CR.string.grades_scoring))
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
                Text(stringResource(CR.string.private_comments))
            }
        }

        item(key = "new_private_comment") {
            UstadAddCommentListItem(
                text = stringResource(id = CR.string.add_private_comment),
                enabled = uiState.fieldsEnabled,
                personUid = uiState.activeUserPersonUid,
                onClickAddComment =  onClickNewPrivateComment,
            )
        }

        items(
            count = privateCommentsLazyPagingItems.itemCount,
            key = privateCommentsLazyPagingItems.itemKey { Pair(Comments.TABLE_ID, it.comment.commentsUid) }
        ) { index ->
            CommentListItem(commentAndName = privateCommentsLazyPagingItems[index])
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
            CourseAssignmentMarkAndMarkerName(
                courseAssignmentMark = CourseAssignmentMark().apply {
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camMark = 8.1f
                    camPenalty = 0.9f
                    camMaxMark = 10f
                    camLct = systemTimeInMillis()
                },
                markerFirstNames = "John",
                markerLastName = "Smith",
            )
        ),
        privateCommentsList = {
            ListPagingSource(listOf(
                CommentsAndName(
                    comment = Comments().apply {
                        commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                    },
                    firstNames = "Bob",
                    lastName = "Dylan"
                )
            ))
        },
    )


    MdcTheme {
        ClazzAssignmentDetailStudentProgressScreen(uiStateVal)
    }
}