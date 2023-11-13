package com.ustadmobile.libuicompose.view.timezone

import androidx.compose.desktop.ui.tooling.preview.Preview
import com.ustadmobile.core.viewmodel.timezone.TimezoneListUiState
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone as TimeZoneKt

@Composable
@Preview
fun TimeZoneListScreenPreview() {
    TimeZoneListScreen(
        uiState = TimezoneListUiState(
            timeZoneList = listOf(
                TimeZoneKt.of("Pacific/Apia"),
                TimeZoneKt.of("Pacific/Midway"),
                TimeZoneKt.of("Pacific/Niue"),
                TimeZoneKt.of("Pacific/Samoa")
            )
        ),
        onListItemClick = {}
    )
}