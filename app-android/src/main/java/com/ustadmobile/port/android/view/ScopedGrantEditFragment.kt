package com.ustadmobile.port.android.view

import android.view.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.MR
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import dev.icerock.moko.resources.compose.stringResource as mrStringResource


interface ScopedGrantEditFragmentEventHandler {

}

class ScopedGrantEditFragment: UstadBaseMvvmFragment() {

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantEditScreen(
    uiState: ScopedGrantEditUiState = ScopedGrantEditUiState(),
    onChangedBitmask: (BitmaskFlag?) -> Unit = {},
) {
    LazyColumn {
        items(
            items = uiState.bitmaskList,
            key = { it.flagVal }
        ) { bitmask ->
            ListItem(
                modifier = Modifier.toggleable(
                    role = Role.Switch,
                    value = bitmask.enabled,
                    onValueChange = {
                        onChangedBitmask(bitmask.copy(enabled = it))
                    }
                ),
                text = { Text(mrStringResource(bitmask.stringResource)) },
                trailing =  {
                    Switch(
                        onCheckedChange = {},
                        checked = bitmask.enabled,
                    )
                }
            )
        }
    }
}

@Composable
@Preview
fun ScopedGrantEditScreenPreview() {
    val uiState = ScopedGrantEditUiState(
        bitmaskList = listOf(
            BitmaskFlag(
                stringResource = MR.strings.permission_person_insert,
                flagVal = 1,
                enabled = true,
            ),
            BitmaskFlag(
                stringResource = MR.strings.permission_person_update,
                flagVal = 2
            )
        )
    )

    MdcTheme {
        ScopedGrantEditScreen(uiState)
    }
}