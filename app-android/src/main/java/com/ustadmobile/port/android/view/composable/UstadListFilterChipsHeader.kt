package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.port.android.util.compose.messageIdResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadListFilterChipsHeader(
    modifier: Modifier = Modifier,
    filterOptions: List<MessageIdOption2>,
    selectedChipId: Int,
    enabled: Boolean = true,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
){

    Row (
        modifier = modifier.horizontalScroll(
            rememberScrollState()
        )
    ){
        filterOptions.forEachIndexed { index, filterOption ->
            FilterChip(
                selected = filterOption.value == selectedChipId,
                onClick = { onClickFilterChip(filterOption) },
                enabled = enabled,
                modifier = Modifier
                    .padding(
                        start = if(index == 0) 16.dp else 8.dp,
                        end = if(index == filterOptions.size - 1) 16.dp else 8.dp
                    ),
            ) {
                Text(messageIdResource(id = filterOption.messageId))
            }
        }
    }
}

@Composable
@Preview
private fun UstadListFilterChipsHeaderPreview() {
    UstadListFilterChipsHeader(
        filterOptions = listOf(
            MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
            MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
            MessageIdOption2(MessageID.all, 0),
        ),
        selectedChipId = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
    )
}
