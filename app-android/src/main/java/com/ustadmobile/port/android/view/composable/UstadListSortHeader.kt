package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.SortHeaderUiState
import com.ustadmobile.port.android.util.compose.messageIdResource

@Composable
fun UstadListSortHeader(
    uiState: SortHeaderUiState,
    onClickSort: () -> Unit = {}
){

    val sortIcon = if(uiState.sortOption?.order == true)
        Icons.Default.ArrowDownward
    else
        Icons.Default.ArrowUpward

    Surface(
        modifier = Modifier.clickable {
            onClickSort()
        }
    ) {
        Row {

            Text(messageIdResource(id = uiState.sortOption?.fieldMessageId
                ?: MessageID.field_person_age))

            Icon(
               sortIcon,
               contentDescription = ""
            )
        }
    }
}

@Composable
@Preview
private fun UstadListSortHeaderPreview() {
    val uiState = SortHeaderUiState(
        sortOption = SortOrderOption(
            MessageID.name,
            ClazzDaoCommon.SORT_CLAZZNAME_ASC,
            true
        )
    )
    UstadListSortHeader(uiState)
}
