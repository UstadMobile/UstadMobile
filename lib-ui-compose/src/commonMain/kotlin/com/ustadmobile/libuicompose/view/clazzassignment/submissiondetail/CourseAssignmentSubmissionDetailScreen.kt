package com.ustadmobile.libuicompose.view.clazzassignment.submissiondetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.clazzassignment.submissiondetail.CourseAssignmentSubmissionDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.submissiondetail.CourseAssignmentSubmissionDetailViewModel
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun CourseAssignmentSubmissionDetailScreen(
    viewModel: CourseAssignmentSubmissionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        CourseAssignmentSubmissionDetailUiState()
    )

    CourseAssignmentSubmissionDetailScreen(uiState)
}

@Composable
fun CourseAssignmentSubmissionDetailScreen(
    uiState: CourseAssignmentSubmissionDetailUiState
) {
    val timeZone = remember {
        TimeZone.currentSystemDefault().id
    }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
    ) {
        val receivedTimeFormatted = rememberFormattedDateTime(
            uiState.submission?.casTimestamp ?: 0,
            timeZoneId = timeZone
        )
        ListItem(
            leadingContent = {
                 Icon(Icons.Default.Schedule, contentDescription = null)
            },
            headlineContent = {
                Text(receivedTimeFormatted)
            },
            supportingContent = {
                Text(stringResource(MR.strings.time_submitted))
            }
        )

        UstadHtmlText(
            html = uiState.submission?.casText ?: "",
            modifier = Modifier.defaultItemPadding()
        )
    }
}