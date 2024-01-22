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
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.util.SortOrderOption
import dev.icerock.moko.resources.compose.stringResource as mrStringResource
import com.ustadmobile.core.R as CR
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

        Text(mrStringResource(resource = activeSortOrderOption.fieldMessageId))

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = if(activeSortOrderOption.order)
                Icons.Default.ArrowDownward
            else
                Icons.Default.ArrowUpward,
            contentDescription = stringResource(if(activeSortOrderOption.order) {
                CR.string.ascending
            }else {
                CR.string.descending
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
        MR.strings.name_key,
        ClazzDaoCommon.SORT_CLAZZNAME_ASC,
        true
    ))
}
