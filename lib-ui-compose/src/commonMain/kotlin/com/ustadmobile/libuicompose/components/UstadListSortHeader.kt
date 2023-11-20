package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    showPopup: Boolean = false,
    sortOptions: List<SortOrderOption> = emptyList(),
    onClick: () -> Unit = {},
    onClickSortOption: (SortOrderOption) -> Unit = { },
){
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
    ) {
        TextButton(
            enabled = enabled,
            onClick = {
                if(showPopup) {
                    expanded = true
                }else {
                    onClick()
                }
            }
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

            if(showPopup) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sortOptions.forEach { sortOption ->
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onClickSortOption(sortOption)
                            },
                            text = {
                                Text(
                                    stringResource(sortOption.fieldMessageId) + " (" + if(sortOption.order) {
                                        stringResource(MR.strings.ascending)
                                    }else {
                                        stringResource(MR.strings.descending)
                                    } + ")"
                                )
                            }
                        )
                    }
                }
            }

        }
    }
}
