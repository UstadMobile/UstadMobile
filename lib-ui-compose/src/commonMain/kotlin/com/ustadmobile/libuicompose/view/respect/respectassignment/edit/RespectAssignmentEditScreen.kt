package com.ustadmobile.libuicompose.view.respect.respectassignment.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.respect.respectassignment.edit.RespectAssignmentEditUiState
import com.ustadmobile.core.viewmodel.respect.respectassignment.edit.RespectAssignmentEditViewModel
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.libuicompose.components.UstadDateTimeField
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone

@Composable
fun RespectAssignmentEditScreen(
    viewModel: RespectAssignmentEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(
        RespectAssignmentEditUiState(), Dispatchers.Main.immediate,
    )

    RespectAssignmentEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
    )
}

@Composable
fun RespectAssignmentEditScreen(
    uiState: RespectAssignmentEditUiState,
    onEntityChanged: (RespectAssignment?) -> Unit = { },
) {
    UstadVerticalScrollColumn(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = uiState.assignment?.razTitle ?: "",
            label = { Text(stringResource(MR.strings.title)) },
            onValueChange = {
                onEntityChanged(uiState.assignment?.copy(razTitle = it))
            },
            maxLines = 1,
        )

        OutlinedTextField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = uiState.assignment?.razDescription ?: "",
            onValueChange = {
                onEntityChanged(uiState.assignment?.copy(razDescription = it))
            },
            label = { Text(stringResource(MR.strings.description)) },
        )

        UstadDateTimeField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth().testTag("deadline"),
            value = uiState.assignment?.razDeadline ?: 0,
            unsetDefault = UNSET_DISTANT_FUTURE,
            dateLabel = { Text(stringResource(MR.strings.deadline)) },
            timeLabel = { stringResource(MR.strings.time) },
            timeZoneId = TimeZone.currentSystemDefault().id,
            onValueChange = {
                onEntityChanged(uiState.assignment?.copy(razDeadline = it))
            },
            baseTestTag = "deadline"
        )
    }
}

