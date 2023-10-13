package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.SortOrderOption
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun UstadListSortHeader(
    activeSortOrderOption: SortOrderOption,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClickSort: () -> Unit = {}
){
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = { onClickSort() }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(stringResource(resource = activeSortOrderOption.fieldMessageId))

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = if(activeSortOrderOption.order)
                Icons.Default.ArrowDownward
            else
                Icons.Default.ArrowUpward,
            contentDescription = stringResource(if(activeSortOrderOption.order) {
                MR.strings.ascending
            }else {
                MR.strings.descending
            }),
            modifier = Modifier.size(16.dp)
        )

    }
}
