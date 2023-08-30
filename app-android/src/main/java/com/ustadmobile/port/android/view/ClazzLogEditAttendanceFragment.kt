package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.google.accompanist.pager.*
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.android.material.button.MaterialButtonToggleGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceUiState
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import kotlinx.coroutines.launch
import java.util.*
import com.ustadmobile.core.R as CR

class ClazzLogEditAttendanceFragment: UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzLogEditAttendanceViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                ClazzLogEditAttendanceScreen(viewModel)
            }
        }
    }

}

@Composable
private fun ClazzLogEditAttendanceScreen(
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
private fun ClazzLogEditAttendanceScreen(
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
                text = { Text(stringResource(id = CR.string.mark_all_present)) },
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
                text = { Text(stringResource(id = CR.string.mark_all_absent)) },
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

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PagerView(
    list: List<ClazzLog>,
    uiStateIndex: Int,
    timeZone: String,
    onChangeClazzLog: (ClazzLog) -> Unit = {},
) {
    val pagerState = rememberPagerState(0)
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
                contentDescription = stringResource(CR.string.previous),
            )
        }

        HorizontalPager(
            modifier = Modifier.weight(8F),
            state = pagerState,
            count = list.size
        ) { index ->

            val dateFormatted = rememberFormattedDateTime(
                timeInMillis = list[index].logDate,
                timeZoneId = timeZone
            )

            Text(dateFormatted)
        }

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
                contentDescription = stringResource(CR.string.next),
            )
        }
    }
}

private val buttonsIdMap = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
    ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button
)

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
                painter = painterResource(id = R.drawable.ic_person_black_24dp),
                contentDescription = ""
            )
        },
        trailing = {
            fun MaterialButtonToggleGroup.update() {
                buttonsIdMap.forEach { (status, buttonId) ->
                    val button = findViewById<Button>(buttonId)
                    button.isEnabled = fieldsEnabled

                    button.setOnClickListener {
                        onClazzLogAttendanceChanged(
                            clazzLog.copy(
                                attendanceRecord = clazzLog.attendanceRecord?.shallowCopy {
                                    attendanceStatus = status
                                }
                            )
                        )
                    }
                }

                val idToCheck = buttonsIdMap[clazzLog.attendanceRecord?.attendanceStatus ?: 0]
                if(idToCheck != null) {
                    check(idToCheck)
                }else {
                    clearChecked()
                }
            }

            AndroidView(
                factory = {  context ->
                    val view = LayoutInflater.from(context).inflate(
                        R.layout.item_clazz_log_attendance_status_toggle_buttons,
                        null, false
                    ) as MaterialButtonToggleGroup

                    view.isSingleSelection = true
                    view.update()


                    view
                },
                update = {
                    it.update()
                }
            )
        }
    )
}

@Composable
@Preview
fun ClazzLogEditAttendanceScreenPreview() {
    val uiState = ClazzLogEditAttendanceUiState(
        clazzLogAttendanceRecordList = listOf(
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 0
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                }
            ),
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 1
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                }
            ),
            PersonAndClazzLogAttendanceRecord(
                person = Person().apply {
                    firstNames = "Student Name"
                },
                attendanceRecord = ClazzLogAttendanceRecord().apply {
                    clazzLogAttendanceRecordUid = 2
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ABSENT
                }
            )
        ),
        clazzLogsList = listOf(
            ClazzLog().apply {
                logDate = 1671629979000
            },
            ClazzLog().apply {
                logDate = 1655608510000
            },
            ClazzLog().apply {
                logDate = 1671975579000
            }
        )
    )

    MdcTheme {
        ClazzLogEditAttendanceScreen(uiState)
    }
}