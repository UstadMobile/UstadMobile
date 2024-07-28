package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UstadListFilterChipsHeader(
    filterOptions: List<MessageIdOption2>,
    selectedChipId: Int,
    enabled: Boolean = true,
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
){
    Box(Modifier.padding(start = 16.dp, end= 16.dp).fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            filterOptions.forEachIndexed { index, filterOption ->
                FilterChip(
                    selected = filterOption.value == selectedChipId,
                    onClick = { onClickFilterChip(filterOption) },
                    enabled = enabled,
                    label = {
                        Text(stringResource(resource = filterOption.stringResource))
                    }
                )
            }
        }
    }

}
