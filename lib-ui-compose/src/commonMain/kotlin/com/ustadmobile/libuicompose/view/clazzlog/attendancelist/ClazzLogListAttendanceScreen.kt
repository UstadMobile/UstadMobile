package com.ustadmobile.libuicompose.view.clazzlog.attendancelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.schedule.totalAttendeeStatusRecorded
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceUiState
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClazzLogListAttendanceScreen(
    viewModel: ClazzLogListAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzLogListAttendanceUiState())

    if(uiState.createNewOptionsVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onDismissCreateNewOptions()
            }
        ) {

            uiState.recordAttendanceOptions.forEach { option ->
                UstadBottomSheetOption(
                    modifier = Modifier.clickable {
                        viewModel.onClickRecordAttendance(option)
                    },
                    headlineContent = { Text(stringResource(option.stringResource)) },
                )
            }
        }
    }

    ClazzLogListAttendanceScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickClazz = viewModel::onClickEntry,
    )
}

@Composable
fun ClazzLogListAttendanceScreen(
    uiState: ClazzLogListAttendanceUiState = ClazzLogListAttendanceUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickClazz: (ClazzLog) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    val repositoryPagerResult = rememberDoorRepositoryPager(
        uiState.clazzLogsList, refreshCommandFlow
    )

    val lazyPagingItems = repositoryPagerResult.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        if(repositoryPagerResult.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { clazzLog -> clazzLog.clazzLogUid }
        ){ clazzLog ->
            ClazzLogListItem(
                clazzLog = clazzLog,
                timeZoneId = uiState.timeZoneId,
                onClick = onClickClazz
            )
        }
    }
}

@Composable
private fun ClazzLogListItem(
    clazzLog: ClazzLog?,
    timeZoneId: String,
    onClick: (ClazzLog) -> Unit,
){

    val dateTime = rememberFormattedDateTime(
        timeInMillis = clazzLog?.logDate ?: 0,
        timeZoneId = timeZoneId,
    )

    ListItem(
        modifier = Modifier.clickable {
            clazzLog?.also(onClick)
        },
        leadingContent = {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = ""
            )
        },
        headlineContent = { Text(dateTime) },
        supportingContent = {
            Column {
                Row {
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumPresent ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        color = colorResource(MR.colors.success),
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumPartial ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumAbsent ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Text(text = stringResource(
                    MR.strings.three_num_items_with_name_with_comma,
                    clazzLog?.clazzLogNumPresent ?: 0,
                    stringResource(MR.strings.present),
                    clazzLog?.clazzLogNumPartial ?: 0,
                    stringResource(MR.strings.partial),
                    clazzLog?.clazzLogNumAbsent ?: 0,
                    stringResource(MR.strings.absent)
                ))
            }
        }
    )
}

@Composable
private fun RowScope.ClazzLogListItemAttendanceStatusBox(
    numerator: Int,
    denominator: Int,
    color: Color,
) {
    if(numerator > 0) {
        Box(modifier = Modifier
            .weight((numerator * 100).toFloat() / max(denominator, 1))
            .height(6.dp)
            .background(color = color)
        )
    }
}