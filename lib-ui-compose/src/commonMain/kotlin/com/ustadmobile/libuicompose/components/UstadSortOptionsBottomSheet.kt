package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.SortOrderOption
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadSortOptionsBottomSheet(
    sortOptions: List<SortOrderOption> = emptyList(),
    onClickSortOption: (SortOrderOption) -> Unit = { },
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            text = stringResource(MR.strings.sort_by)
        )

        Divider(thickness = 1.dp)

        Column(
            Modifier.verticalScroll(
                state = rememberScrollState()
            ).fillMaxSize()
        ) {
            sortOptions.forEach { sortOption ->
                UstadBottomSheetOption(
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        onClickSortOption(sortOption)
                    },
                    headlineContent = {
                        Text(
                            stringResource(sortOption.fieldMessageId) + " (" + if(sortOption.order) {
                                stringResource(MR.strings.ascending)
                            }else {
                                stringResource(MR.strings.descending)
                            } + ")"
                        )
                    },
                )
            }
        }
    }

}