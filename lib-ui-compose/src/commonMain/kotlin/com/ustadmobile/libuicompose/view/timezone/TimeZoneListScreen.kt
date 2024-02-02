package com.ustadmobile.libuicompose.view.timezone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.formattedString
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.timezone.TimezoneListUiState
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone as TimeZoneKt

@Composable
fun TimeZoneListScreen(
    viewModel: TimeZoneListViewModel
) {
    val uiState: TimezoneListUiState by viewModel.uiState.collectAsState(TimezoneListUiState())
    TimeZoneListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry
    )
}
@Composable
fun TimeZoneListScreen(
    uiState: TimezoneListUiState,
    onListItemClick: (TimeZoneKt) -> Unit,
) {

    val timeNow = Clock.System.now()

    UstadLazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        items(
            items = uiState.timeZoneList,
            key = { it.id },
        ) { timeZone ->
            val timeZoneFormatted: String = remember(timeZone.id) {
                timeZone.formattedString(timeNow)
            }

            ListItem(
                modifier = Modifier
                    .clickable {  onListItemClick(timeZone) } ,
                headlineContent = { Text(timeZoneFormatted) }
            )
        }
    }
}