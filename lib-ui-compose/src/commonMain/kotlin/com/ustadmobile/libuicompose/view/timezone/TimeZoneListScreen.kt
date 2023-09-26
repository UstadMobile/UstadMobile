package com.ustadmobile.libuicompose.view.timezone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.formattedString
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.timezone.TimezoneListUiState
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone as TimeZoneKt

@Composable
fun TimeZoneListScreenForViewModel(
    viewModel: TimeZoneListViewModel
) {
    val uiState: TimezoneListUiState by viewModel.uiState.collectAsState(TimezoneListUiState())
    TimeZoneListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimeZoneListScreen(
    uiState: TimezoneListUiState,
    onListItemClick: (TimeZoneKt) -> Unit,
) {

    val timeNow = Clock.System.now()

    LazyColumn(
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
                text = { Text(timeZoneFormatted) }
            )
        }
    }
}