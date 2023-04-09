package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.lib.db.entities.DeviceSession
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NetworkNodeListScreen(
    uiState: NetworkNodeListUiState = NetworkNodeListUiState(),
    onChangeBluetoothEnabled: (Boolean) -> Unit = {},
    onChangeHotspotEnabled: (Boolean) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickDevice: (DeviceSession) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            UstadDetailField(
                valueText = uiState.deviceNumber,
                labelText = stringResource(R.string.device),
                imageId = R.drawable.ic_phone_black_24dp,
            )
        }

        item {
            UstadSwitchField(
                checked = uiState.fieldsEnabled,
                label = stringResource(id = R.string.offline_sharing_enable_bluetooth_prompt),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeBluetoothEnabled(it)
                }
            )
        }

        item {
            UstadSwitchField(
                checked = uiState.fieldsEnabled,
                label = stringResource(id = R.string.offline_sharing_enable_wifi_promot),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeHotspotEnabled(it)
                }
            )
        }

        item {
            Text("${stringResource(R.string.download_wifi_only)}: ${uiState.wifiName}")
        }


        item {
            Divider()
        }

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.deviceFilterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        items(
            items = uiState.devices,
            key = { device -> device.dsDeviceId }
        ){ device ->
            ListItem(
                modifier = Modifier.clickable {
                        onClickDevice(device)
                    },

                text = { Text("Phone Number") },
                icon = {
                    LeadingContent(device = device)
                },
                secondaryText = { Text("Server") }
            )
        }

    }
}

@Composable
private fun LeadingContent(
    device: DeviceSession
){

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ){
        Icon(
            Icons.Default.Bluetooth,
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(45.dp)
                .padding(4.dp),
        )

        Box(
            modifier = Modifier
                .width(45.dp)
                .height(15.dp)
                .padding(end = 10.dp)
        ) {
            LinearProgressIndicator(
                progress = (device.expires/100.0)
                    .toFloat(),
                modifier = Modifier
                    .height(4.dp)
            )
        }

    }
}

@Composable
@Preview
fun NetworkNodeListPreview() {
    val uiStateVal = NetworkNodeListUiState(
        devices = listOf(
            DeviceSession().apply {
                dsDeviceId = 1
            }
        ),
        deviceNumber = "+12341231",
        wifiName = "LocalSpot1231"
    )

    MdcTheme {
        NetworkNodeListScreen(uiStateVal)
    }
}