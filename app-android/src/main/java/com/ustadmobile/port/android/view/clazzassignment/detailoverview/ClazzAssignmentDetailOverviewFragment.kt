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
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.clazzassignment.CommentsBottomSheet
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewFragment.Companion.SUBMISSION_POLICY_MAP
import com.ustadmobile.port.android.view.composable.UstadAddCommentListItem
import com.ustadmobile.port.android.view.clazzassignment.CommentListItem
import com.ustadmobile.port.android.view.clazzassignment.UstadAssignmentSubmissionHeader
import com.ustadmobile.port.android.view.clazzassignment.UstadCourseAssignmentMarkListItem
import com.ustadmobile.port.android.view.composable.*
import java.util.*
import com.ustadmobile.core.R as CR
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreenForViewModel
import com.ustadmobile.port.android.util.compose.stringIdMapResource
import dev.icerock.moko.resources.compose.stringResource as mrStringResource

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
                    ClazzAssignmentDetailOverviewScreenForViewModel(viewModel)
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