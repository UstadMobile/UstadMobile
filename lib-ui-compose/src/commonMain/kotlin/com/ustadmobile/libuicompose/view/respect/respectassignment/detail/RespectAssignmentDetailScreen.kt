package com.ustadmobile.libuicompose.view.respect.respectassignment.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.respect.respectassignment.detail.RespectAssignmentDetailUiState
import com.ustadmobile.core.viewmodel.respect.respectassignment.detail.RespectAssignmentDetailViewModel
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.TimeZone
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadLazyColumn

@Composable
fun RespectAssignmentDetailScreen(
    viewModel: RespectAssignmentDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(RespectAssignmentDetailUiState())

    RespectAssignmentDetailScreen(
        uiState = uiState,
        onClickLaunch = viewModel::onClickLaunch,
    )
}

@Composable
fun RespectAssignmentDetailScreen(
    uiState: RespectAssignmentDetailUiState,
    onClickLaunch: () -> Unit,
) {
    val deadlineDateAndTime = rememberFormattedDateTime(
        timeInMillis = uiState.assignment?.assignment?.razDeadline ?: 0,
        timeZoneId = TimeZone.currentSystemDefault().id,
    )

    UstadLazyColumn(modifier = Modifier.fillMaxSize()) {
        item("details") {
            Text(
                text = uiState.assignment?.assignment?.razDescription ?: "",
                modifier = Modifier.defaultItemPadding().fillMaxWidth()
            )

            UstadDetailField2(
                modifier = Modifier.fillMaxWidth(),
                valueContent = {
                    Text(text = deadlineDateAndTime)
                },
                labelContent = {
                    Text(stringResource(MR.strings.deadline))
                }
            )
        }

        item("launchbutton") {
            Button(
                onClick = onClickLaunch,
                modifier = Modifier.defaultItemPadding().fillMaxWidth()
            ) {
                Text(stringResource(MR.strings.open))
            }
        }
    }
}
