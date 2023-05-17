package com.ustadmobile.port.android.view.clazzassignment.detailoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdMapResource
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.clazzassignment.CommentsBottomSheet
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewFragment.Companion.SUBMISSION_POLICY_MAP
import com.ustadmobile.port.android.view.clazzassignment.AddCommentListItem
import com.ustadmobile.port.android.view.clazzassignment.CommentListItem
import com.ustadmobile.port.android.view.clazzassignment.UstadAssignmentSubmissionHeader
import com.ustadmobile.port.android.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.port.android.view.composable.*
import java.util.*


interface ClazzAssignmentDetailOverviewFragmentEventHandler {

    fun onSubmitButtonClicked()

    fun onAddFileClicked()

    fun onAddTextClicked()

}

class ClazzAssignmentDetailOverviewFragment : UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzAssignmentDetailOverviewViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentDetailOverviewScreen(viewModel)
                }
            }
        }
    }

    companion object {

        @JvmField
        val ASSIGNMENT_STATUS_MAP = mapOf(
                CourseAssignmentSubmission.NOT_SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.MARKED to R.drawable.ic_baseline_done_all_24
        )

        @JvmField
        val SUBMISSION_POLICY_MAP = mapOf(
            ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to R.drawable.ic_baseline_task_alt_24,
            ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to R.drawable.ic_baseline_add_task_24,
        )


    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzAssignmentDetailOverviewScreen(
    uiState: ClazzAssignmentDetailOverviewUiState,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickMark: (CourseAssignmentMarkWithPersonMarker?) -> Unit = {},
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
    }?.messageId ?: MessageID.submit_all_at_once_submission_policy


    val caFileType = messageIdMapResource(
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
                HtmlText(
                    html = uiState.courseBlock?.cbDescription ?: "",
                    modifier = Modifier.defaultItemPadding()
                )
            }
        }

        if (uiState.cbDeadlineDateVisible){
            item {
                UstadDetailField(
                    valueText = "$formattedDateTime (${TimeZone.getDefault().id})",
                    labelText = stringResource(id = R.string.deadline),
                    imageId = R.drawable.ic_event_available_black_24dp,
                    onClick = {  }
                )
            }
        }

        item {
            UstadDetailField(
                valueText = messageIdResource(policyMessageId),
                labelText = stringResource(id = R.string.submission_policy),
                imageId = SUBMISSION_POLICY_MAP[uiState.assignment?.caSubmissionPolicy]
                    ?: R.drawable.ic_baseline_task_alt_24,
                onClick = {  }
            )
        }

        item {
            UstadAssignmentSubmissionHeader(
                uiState = uiState.submissionHeaderUiState,
            )
        }

        if(uiState.activeUserIsSubmitter) {
            item {
                UstadEditHeader(text = stringResource(R.string.your_submission))
            }

            item {
                if(uiState.activeUserCanSubmit) {
                    HtmlClickableTextField(
                        modifier = Modifier
                            .testTag("submission_text_field")
                            .fillMaxWidth(),
                        html = uiState.latestSubmission?.casText ?: "",
                        label = stringResource(R.string.text),
                        onClick = onClickEditSubmission
                    )
                }else {
                    HtmlText(
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
                        text = { Text(stringResource(R.string.add_file)) },
                        secondaryText = {
                            Text(
                                "${stringResource(R.string.file_type_chosen)} $caFileType" +
                                stringResource(R.string.max_number_of_files,
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
                        backgroundColor = colorResource(id = R.color.secondaryColor)
                    )
                ) {
                    Text(stringResource(R.string.submit).uppercase(),
                        color = contentColorFor(
                            colorResource(id = R.color.secondaryColor)
                        )
                    )
                }
            }
        }


        if(uiState.activeUserIsSubmitter) {
            item {
                ListItem(
                    text = { Text(stringResource(R.string.grades_class_age)) }
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
                key = { Pair(3, it.camUid) }
            ){ mark ->
                UstadCourseAssignmentMarkListItem(
                    onClickMark = onClickMark,
                    uiState = UstadCourseAssignmentMarkListItemUiState(
                        mark = mark,
                        block = uiState.courseBlock ?: CourseBlock()
                    ),
                )
            }
        }

        item {
            ListItem(
                text = {Text(stringResource(R.string.class_comments))}
            )
        }

        item {
            AddCommentListItem(
                text = stringResource(id = R.string.add_class_comment),
                enabled = uiState.fieldsEnabled,
                personUid = 0,
                onClickAddComment = { onClickNewPublicComment() }
            )
        }

        items(
            items = courseCommentsLazyPagingItems,
            key = { Pair(4, it.comment.commentsUid) }
        ){ comment ->
            CommentListItem(commentAndName = comment)
        }

        if(uiState.activeUserIsSubmitter) {
            item {
                ListItem(
                    text = {Text(stringResource(R.string.private_comments))}
                )
            }

            item {
                AddCommentListItem(
                    text = stringResource(id = R.string.add_private_comment),
                    enabled = uiState.fieldsEnabled,
                    personUid = 0,
                    onClickAddComment = { onClickNewPrivateComment() }
                )
            }

            items(
                items = privateCommentsLazyPagingItems,
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

@Composable
fun ClazzAssignmentDetailOverviewScreen(viewModel: ClazzAssignmentDetailOverviewViewModel) {
    val uiState by viewModel.uiState.collectAsState(initial = ClazzAssignmentDetailOverviewUiState())

    val localContext = LocalContext.current
    val newCourseCommentHint = stringResource(id = R.string.add_class_comment)
    val newPrivateCommentHint = stringResource(id = R.string.add_private_comment)

    ClazzAssignmentDetailOverviewScreen(
        uiState = uiState,
        onClickEditSubmission = viewModel::onClickEditSubmissionText,
        onClickNewPublicComment = {
            CommentsBottomSheet(
                hintText = newCourseCommentHint,
                personUid = uiState.activeUserPersonUid,
                onSubmitComment = {
                    viewModel.onChangeCourseCommentText(it)
                    viewModel.onClickSubmitCourseComment()
                }
            ).show(localContext.getContextSupportFragmentManager(), "public_comment_sheet")
        },
        onClickNewPrivateComment = {
            CommentsBottomSheet(
                hintText = newPrivateCommentHint,
                personUid = uiState.activeUserPersonUid,
                onSubmitComment = {
                    viewModel.onChangePrivateCommentText(it)
                    viewModel.onClickSubmitCourseComment()
                }
            ).show(localContext.getContextSupportFragmentManager(), "private_comment_sheet")
        },
        onClickSubmitSubmission = viewModel::onClickSubmit
    )
}

@Composable
@Preview
fun ClazzAssignmentDetailOverviewScreenPreview(){

    val uiState = ClazzAssignmentDetailOverviewUiState(
        assignment = ClazzAssignment().apply {
            caRequireTextSubmission = true
        },
        courseBlock = CourseBlock().apply {
            cbDeadlineDate = 1685509200000L
            cbDescription = "Complete your assignment or <b>else</b>"
        },
        submitterUid = 42L,
        addFileVisible = true,
        submissionTextFieldVisible = true,
        latestSubmissionAttachments = listOf(
            CourseAssignmentSubmissionAttachment().apply {
                casaUid = 1L
                casaFileName = "File.pdf"
            },
        ),
        latestSubmission = CourseAssignmentSubmission().apply {
            casText = ""
        },
        markList = listOf(
            CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                }
            }
        ),
        courseComments = {
            ListPagingSource(listOf(
                CommentsAndName().apply {
                    comment = Comments().apply {
                        commentsUid = 1
                        commentsText = "This is a very difficult assignment."
                    }
                    firstNames = "Bob"
                    lastName = "Dylan"
                }
            ))
        },
        privateComments = {
            ListPagingSource(
                listOf(
                    CommentsAndName().apply {
                        comment = Comments().apply {
                            commentsUid = 2
                            commentsText = "Can I please have extension? My rabbit ate my homework."
                        }
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                ),
            )
        },
    )

    MdcTheme {
        ClazzAssignmentDetailOverviewScreen(uiState)
    }
}