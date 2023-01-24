package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.port.android.util.ext.defaultItemPadding

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAddListItem(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Filled.Add,
    onClickAdd: (() -> Unit) = {  }
){
    ListItem(
        modifier = modifier
            .defaultItemPadding()
            .clickable(
                enabled = enabled,
                onClick = { onClickAdd() }
            ),
        icon = {
            Icon(
                icon,
                contentDescription = null
            )
        },
        text = {
            Text(text)}
    )
}

@Composable
@Preview
private fun UstadAddListItemPreview() {
    MdcTheme {
        UstadAddListItem(
            text = "Add",
            enabled = true,
            icon = Icons.Default.Add,
            onClickAdd = {}
        )
    }
}
