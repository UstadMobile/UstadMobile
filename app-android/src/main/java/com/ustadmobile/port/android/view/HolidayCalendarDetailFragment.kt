@file:OptIn(ExperimentalMaterialApi::class)

package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.HolidayCalendarDetailUIState
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.core.R as CR

@Composable
fun HolidayCalendarDetailScreen(
    uiState: HolidayCalendarDetailUIState,
    onItemClick: (Holiday) -> Unit = {}
){
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        UstadDetailField(
            valueText = uiState.holidayCalendar?.umCalendarName ?: "",
            labelText = stringResource(id = CR.string.name_key),
            imageId = R.drawable.ic_calendar_today_24px_,
        )

        uiState.holidayList.forEach { holiday ->

            val holidayStart = rememberFormattedDate(holiday.holStartTime, "UTC")
            val holidayEnd = rememberFormattedDate(holiday.holEndTime, "UTC")

            ListItem (
                modifier = Modifier
                    .clickable {
                        onItemClick(holiday)
                    },

                text = { Text(holiday.holName ?: "") },
                
                secondaryText = {
                    Text(text = "$holidayStart - $holidayEnd")
                }
            )
        }

    }
}

@Composable
@Preview
fun HolidayCalendarDetailPreview(){
    HolidayCalendarDetailScreen(
        uiState = HolidayCalendarDetailUIState(
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "my calendar"
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