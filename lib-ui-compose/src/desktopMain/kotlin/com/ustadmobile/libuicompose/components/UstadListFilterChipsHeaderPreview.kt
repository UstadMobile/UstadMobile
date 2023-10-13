package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import com.ustadmobile.core.util.MessageIdOption2

@Composable
@Preview
private fun UstadListFilterChipsHeaderPreview() {
    UstadListFilterChipsHeader(
        filterOptions = listOf(
            MessageIdOption2(MR.strings.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
            MessageIdOption2(MR.strings.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
            MessageIdOption2(MR.strings.all, 0),
        ),
        selectedChipId = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
    )
}