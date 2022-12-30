package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.FilterChipsHeaderUiState
import com.ustadmobile.port.android.util.compose.messageIdResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadListFilterChipsHeader(
    uiState: FilterChipsHeaderUiState,
    onClickFilterChip: (MessageIdOption2) -> Unit = {}
){

    Row (
        modifier = Modifier.horizontalScroll(
            rememberScrollState()
        )
    ){
        uiState.filterOptions.forEach { filterOption ->
            Chip(
                onClick = { onClickFilterChip(filterOption) },
                enabled = uiState.fieldsEnabled,
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                Text(messageIdResource(id = filterOption.messageId))
            }
        }
    }
}

@Composable
@Preview
private fun UstadListFilterChipsHeaderPreview() {
    val uiState = FilterChipsHeaderUiState(
        filterOptions = listOf(
            MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
            MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
            MessageIdOption2(MessageID.all, 0),
        )
    )
    UstadListFilterChipsHeader(uiState)
}
