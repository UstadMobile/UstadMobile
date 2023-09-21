package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.HolidayCalendarEditUiState
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.core.R as CR

class HolidayCalendarEditFragment() : UstadBaseMvvmFragment() {


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HolidayCalendarEditScreen(
    uiState: HolidayCalendarEditUiState,
    onHolidayCalendarChange: (HolidayCalendar?) -> Unit = {},
    onClickAddItem: () -> Unit = {},
    onDeleteItemClick: (Holiday) -> Unit = {},
    onItemClick: (Holiday?) -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        UstadTextEditField(
            value = uiState.holidayCalendar?.umCalendarName ?: "",
            label = stringResource(id = CR.string.name_key),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onHolidayCalendarChange(uiState.holidayCalendar?.shallowCopy {
                    umCalendarName = it
                })
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickAddItem()
            },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(id = CR.string.add_a_holiday)) }
        )

        uiState.holidayList?.forEach { holiday ->

            val holidayStart = rememberFormattedDate(timeInMillis = holiday.holStartTime, timeZoneId = "UTC")
            val holidayEnd = rememberFormattedDate(timeInMillis = holiday.holEndTime, timeZoneId = "UTC")

            ListItem(
                modifier = Modifier
                    .clickable {
                        onItemClick(holiday)
                    },
                icon = {
                    Spacer(modifier = Modifier.width(24.dp))
                },
                text = { Text(text = holiday.holName ?: "") },
                secondaryText = {
                    Text(text = "$holidayStart - $holidayEnd")
                },
                trailing = {
                    IconButton(onClick = {
                        onDeleteItemClick(holiday)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(CR.string.delete)
                        )
                    }
                }
            )
        }

    }
}

@Composable
@Preview
fun HolidayCalendarEditPreview(){
    HolidayCalendarEditScreen(
        uiState = HolidayCalendarEditUiState(
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "my cal"
            },
            holidayList = listOf(
                Holiday().apply {
                    holName = "Eid"
                    holStartTime = 1352958816
                    holEndTime = 1352958818
                }
            )
        )
    )
}
