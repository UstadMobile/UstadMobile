package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadDetailField


@Composable
private fun NetworkNodeListScreen(
    uiState: NetworkNodeListUiState = NetworkNodeListUiState(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            UstadDetailField(
                valueText = "+12341231",
                labelText = stringResource(R.string.device),
                imageId = R.drawable.ic_phone_black_24dp,
            )
        }
    }
}

@Composable
@Preview
fun NetworkNodeListPreview() {
    val uiStateVal = NetworkNodeListUiState()

    MdcTheme {
        NetworkNodeListScreen(uiStateVal)
    }
}