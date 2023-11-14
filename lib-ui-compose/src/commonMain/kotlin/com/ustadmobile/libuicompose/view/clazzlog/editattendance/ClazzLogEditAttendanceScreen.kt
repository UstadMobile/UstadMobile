package com.ustadmobile.libuicompose.view.clazzlog.editattendance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceUiState
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@Composable
fun ClazzLogEditAttendanceScreenForViewModel(
    viewModel: ClazzLogEditAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzLogEditAttendanceUiState())
    ClazzLogEditAttendanceScreen(
        uiState = uiState,
        onClickMarkAll = viewModel::onClickMarkAll,
        onChangeClazzLog = viewModel::onChangeClazzLog,
        onClazzLogAttendanceChanged = viewModel::onClazzLogAttendanceChanged
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClazzLogEditAttendanceScreen(
    uiState: ClazzLogEditAttendanceUiState = ClazzLogEditAttendanceUiState(),
    onClickMarkAll: (status: Int) -> Unit = {},
    onChangeClazzLog: (ClazzLog) -> Unit = {},
    onClazzLogAttendanceChanged: (PersonAndClazzLogAttendanceRecord) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    )  {

        item {
            PagerView (
                list = uiState.clazzLogsList,
                uiStateIndex = uiState.currentClazzLogIndex,
                timeZone = uiState.timeZone,
                onChangeClazzLog = onChangeClazzLog
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)
                },
                text = { Text(stringResource(MR.strings.mark_all_present)) },
                icon = {
                    Icon(
                        Icons.Outlined.LibraryAddCheck,
                        contentDescription = ""
                    )
                }
            )
        }

        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT)
                },
                text = { Text(stringResource(MR.strings.mark_all_absent)) },
                icon = {
                    Icon(
                        Icons.Outlined.CheckBox,
                        contentDescription = ""
                    )
                }
            )
        }

        items(
            items = uiState.clazzLogAttendanceRecordList,
            key = { clazzLog -> clazzLog.person?.personUid ?: 0L }
        ){ clazzLogAttendance ->
            ClazzLogItemView(
                clazzLog = clazzLogAttendance,
                fieldsEnabled = uiState.fieldsEnabled,
                onClazzLogAttendanceChanged = onClazzLogAttendanceChanged
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
//@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PagerView(
    list: List<ClazzLog>,
    uiStateIndex: Int,
    timeZone: String,
    onChangeClazzLog: (ClazzLog) -> Unit = {},
) {

    // TODO error
    val pagerState =  rememberPagerState(pageCount = { 10 })

//    val pagerState = rememberPagerState(0)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState, list) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            list.getOrNull(currentPage)?.also {
                onChangeClazzLog(it)
            }
        }
    }

    LaunchedEffect(uiStateIndex) {
        if(pagerState.currentPage != uiStateIndex) {
            pagerState.scrollToPage(uiStateIndex)
        }
    }

    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){

        IconButton(
            onClick = {
                if (pagerState.currentPage > 0){
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            modifier = Modifier.weight(1F).testTag("prev_day_button"),
            enabled = pagerState.currentPage > 0
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = stringResource(MR.strings.previous),
            )
        }

        HorizontalPager(
            modifier = Modifier.weight(8F),
            state = pagerState
        ) { index ->
            val dateFormatted = rememberFormattedDateTime(
                timeInMillis = list[index].logDate,
                timeZoneId = timeZone
            )

            Text(dateFormatted)
        }
//        HorizontalPager(
//            modifier = Modifier.weight(8F),
//            state = pagerState,
//            count = list.size
//        ) { index ->
//
//            val dateFormatted = rememberFormattedDateTime(
//                timeInMillis = list[index].logDate,
//                timeZoneId = timeZone
//            )
//
//            Text(dateFormatted)
//        }

        IconButton(
            onClick = {
                if(pagerState.currentPage < list.size -1) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.weight(1F).testTag("next_day_button"),
            enabled = pagerState.currentPage < list.size -1
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = stringResource(MR.strings.next),
            )
        }
    }
}

//private val buttonsIdMap = mapOf(
//    ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
//    ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
//    ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button
//)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzLogItemView(
    fieldsEnabled: Boolean,
    clazzLog: PersonAndClazzLogAttendanceRecord,
    onClazzLogAttendanceChanged: (PersonAndClazzLogAttendanceRecord) -> Unit
) {

    ListItem(
        text = {
            Text(text = clazzLog.person?.personFullName() ?: "")
        },
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = ""
            )
        },
//        trailing = {
//            fun MaterialButtonToggleGroup.update() {
//                buttonsIdMap.forEach { (status, buttonId) ->
//                    val button = findViewById<Button>(buttonId)
//                    button.isEnabled = fieldsEnabled
//
//                    button.setOnClickListener {
//                        onClazzLogAttendanceChanged(
//                            clazzLog.copy(
//                                attendanceRecord = clazzLog.attendanceRecord?.shallowCopy {
//                                    attendanceStatus = status
//                                }
//                            )
//                        )
//                    }
//                }
//
//                val idToCheck = buttonsIdMap[clazzLog.attendanceRecord?.attendanceStatus ?: 0]
//                if(idToCheck != null) {
//                    check(idToCheck)
//                }else {
//                    clearChecked()
//                }
//            }
//
//            AndroidView(
//                factory = {  context ->
//                    val view = LayoutInflater.from(context).inflate(
//                        R.layout.item_clazz_log_attendance_status_toggle_buttons,
//                        null, false
//                    ) as MaterialButtonToggleGroup
//
//                    view.isSingleSelection = true
//                    view.update()
//
//
//                    view
//                },
//                update = {
//                    it.update()
//                }
//            )
//        }
    )


}