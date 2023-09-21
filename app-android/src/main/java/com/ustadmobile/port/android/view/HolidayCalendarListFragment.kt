package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.viewmodel.HolidayCalendarListUiState
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.core.R as CR

class HolidayCalendarListFragment(): UstadBaseMvvmFragment() {

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HolidayCalendarListScreen(
    uiState: HolidayCalendarListUiState,
    onListItemClick: (HolidayCalendarWithNumEntries) -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultScreenPadding()
    ){
        items(
            uiState.holidayCalendarList,
            key = {it.umCalendarUid}
        ){ holidayCalendar ->
            ListItem(
                modifier = Modifier
                    .clickable{
                        onListItemClick(holidayCalendar)
                    },
                text = {
                    Text(text = holidayCalendar.umCalendarName ?: "")
                },
                secondaryText = {
                    Text(text = stringResource(id = CR.string.num_holidays, holidayCalendar.numEntries))
                }
            )
        }
    }
}

@Composable
@Preview
fun HolidayCalendarListScreenPreview(){
    HolidayCalendarListScreen(
        uiState = HolidayCalendarListUiState(
            holidayCalendarList = listOf(
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 1"
                    umCalendarUid = 898787
                    numEntries = 4
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 2"
                    umCalendarUid = 8
                    numEntries = 3
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 3"
                    umCalendarUid = 80
                    numEntries = 2
                }
            )
        )
    )
}