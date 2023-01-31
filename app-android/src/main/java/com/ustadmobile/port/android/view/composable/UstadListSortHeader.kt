package com.ustadmobile.port.android.view.composable

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.port.android.util.compose.messageIdResource

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

        Text(messageIdResource(id = activeSortOrderOption.fieldMessageId))

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = if(activeSortOrderOption.order)
                Icons.Default.ArrowDownward
            else
                Icons.Default.ArrowUpward,
            contentDescription = stringResource(if(activeSortOrderOption.order) {
                R.string.ascending
            }else {
                R.string.descending
            }),
            modifier = Modifier.size(16.dp)
        )

    }
}

@Composable
@Preview
private fun UstadListSortHeaderPreview() {
    UstadListSortHeader(
        activeSortOrderOption = SortOrderOption(
        MessageID.name,
        ClazzDaoCommon.SORT_CLAZZNAME_ASC,
        true
    ))
}
