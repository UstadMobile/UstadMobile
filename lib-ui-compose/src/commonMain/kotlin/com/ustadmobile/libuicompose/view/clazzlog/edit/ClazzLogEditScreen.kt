package com.ustadmobile.libuicompose.view.clazzlog.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditUiState
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadDateTimeField
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ClazzLogEditScreen(viewModel: ClazzLogEditViewModel){
    val uiState by viewModel.uiState.collectAsState(ClazzLogEditUiState())
    ClazzLogEditScreen(
        uiState = uiState,
        onChangeClazzLog = viewModel::onEntityChanged,
    )
}

@Composable
fun ClazzLogEditScreen(
    uiState: ClazzLogEditUiState = ClazzLogEditUiState(),
    onChangeClazzLog: (ClazzLog?) -> Unit = {},
) {

    UstadDateTimeField(
        modifier = Modifier
            .defaultItemPadding()
            .fillMaxWidth()
            .testTag("date"),
        value = uiState.clazzLog?.logDate ?: 0L,
        isError = uiState.dateError != null,
        dateLabel = { Text(stringResource(MR.strings.date) + "*") },
        timeLabel = { Text(stringResource(MR.strings.time) + "*") },
        timeZoneId = uiState.timeZone,
        onValueChange = {
            onChangeClazzLog(uiState.clazzLog?.shallowCopy {
                logDate = it
            })
        },
        dateSupportingText = {
            Text(uiState.dateError ?: stringResource(MR.strings.required))
        },
        timeSupportingText = {
            Text(stringResource(MR.strings.required))
        },
        baseTestTag = "date",
    )


}
