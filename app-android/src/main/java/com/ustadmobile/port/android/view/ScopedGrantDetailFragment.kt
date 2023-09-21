package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantDetailUiState
import com.ustadmobile.core.R as CR
import dev.icerock.moko.resources.compose.stringResource as mrStringResource
import com.ustadmobile.core.MR

interface ScopedGrantDetailFragmentEventHandler {

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantDetailScreen(
    uiState: ScopedGrantDetailUiState,
){

    LazyColumn {
        items(
            items = uiState.bitmaskList,
            key = { it.flagVal }
        ) { bitmask ->
            ListItem(
                text = {
                    Text(mrStringResource(resource = bitmask.stringResource))
                },
                trailing = {
                    Icon(
                        imageVector = if (bitmask.enabled) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = stringResource(
                            if(bitmask.enabled) CR.string.enabled else CR.string.disabled
                        )
                    )
                }
            )
        }
    }

}

@Composable
@Preview
fun ScopedGrantDetailScreenPreview(){
    ScopedGrantDetailScreen(
        uiState = ScopedGrantDetailUiState(
            bitmaskList = listOf(
                BitmaskFlag(
                    stringResource = MR.strings.permission_person_update,
                    flagVal = 1,
                    enabled = true
                ),
                BitmaskFlag(
                    stringResource = MR.strings.permission_person_insert,
                    flagVal = 2,
                    enabled = false
                )
            )
        )
    )
}