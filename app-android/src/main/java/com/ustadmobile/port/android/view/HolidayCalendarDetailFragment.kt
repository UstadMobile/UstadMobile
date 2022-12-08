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
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.port.android.view.composable.UstadDetailField

@Composable
fun HolidayCalendarDetailScreen(
    uiState: HolidayCalendarDetailUIState,
    onItemClick: (HolidayCalendar) -> Unit = {}
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UstadDetailField(
            valueText = uiState.holidayCalendar?.umCalendarName ?: "",
            labelText = stringResource(id = R.string.name)
        )

        uiState.calendarList?.forEach {
            ListItem (
                modifier = Modifier
                    .clickable {
                        onItemClick(it)
                    },

                text = { Text(it.umCalendarName ?: "") }
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
            calendarList = listOf(
                HolidayCalendar().apply {
                    umCalendarName = "first"
                }
            )
        )
    )
}