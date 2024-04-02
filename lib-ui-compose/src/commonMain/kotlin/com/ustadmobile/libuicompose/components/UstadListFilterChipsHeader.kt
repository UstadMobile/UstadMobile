package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.compose.stringResource

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
                label = {
                    Text(stringResource(resource = filterOption.stringResource))
                }
            )
        }
    }
}
