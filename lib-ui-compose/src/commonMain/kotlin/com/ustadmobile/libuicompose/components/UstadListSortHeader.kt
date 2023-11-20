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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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

enum class SortListMode {

    POPUP, BOTTOM_SHEET

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadListSortHeader(
    activeSortOrderOption: SortOrderOption,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    mode: SortListMode = SortListMode.BOTTOM_SHEET,
    sortOptions: List<SortOrderOption> = emptyList(),
    onClickSortOption: (SortOrderOption) -> Unit = { },
){
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
    ) {
        TextButton(
            enabled = enabled,
            onClick = {
                expanded = true
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

            when(mode) {
                SortListMode.POPUP -> {
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
                SortListMode.BOTTOM_SHEET -> {
                    if(expanded) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            UstadSortOptionsBottomSheet(
                                sortOptions = sortOptions,
                                onClickSortOption = onClickSortOption,
                                onDismissRequest = {
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
