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
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NetworkNodeListScreen(
    uiState: NetworkNodeListUiState = NetworkNodeListUiState(),
    onChangeBluetoothEnabled: (Boolean) -> Unit = {},
    onChangeHotspotEnabled: (Boolean) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
    onClickNetworkNode: (NetworkNode) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            UstadDetailField(
                modifier = Modifier.defaultItemPadding(),
                valueText = uiState.deviceName,
                labelText = stringResource(R.string.device),
                icon = { Icon(Icons.Default.Smartphone, contentDescription = "") },
            )
        }

        item {
            UstadSwitchField(
                modifier = Modifier.defaultItemPadding(),
                checked = uiState.fieldsEnabled,
                label = stringResource(id = R.string.bluetooth_sharing),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeBluetoothEnabled(it)
                }
            )
        }

        item {
            UstadSwitchField(
                modifier = Modifier.defaultItemPadding(),
                checked = uiState.fieldsEnabled,
                label = stringResource(id = R.string.hotspot_sharing),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeHotspotEnabled(it)
                }
            )
        }

        item {
            Text(modifier = Modifier.defaultItemPadding(),
                text = "${stringResource(R.string.wifi_ssid)}: ${uiState.wifiSSID}")
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
            items = uiState.networkNodes,
            key = { networkNode -> networkNode.nodeId }
        ){ networkNode ->
            ListItem(
                modifier = Modifier.clickable {
                    onClickNetworkNode(networkNode)
                },

                text = { Text("Phone Number") },
                icon = {
                    LeadingContent(networkNode = networkNode)
                },
                secondaryText = { Text("Server") }
            )
        }

    }
}

@Composable
private fun LeadingContent(
    networkNode: NetworkNode
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
                .size(35.dp)
                .padding(4.dp),
        )

        Box(
            modifier = Modifier
                .width(45.dp)
                .height(5.dp)
                .padding(end = 5.dp)
        ) {
            LinearProgressIndicator(
                progress = (networkNode.wifiDirectDeviceStatus/100.0)
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
        networkNodes = listOf(
            NetworkNode().apply {
                nodeId = 1

            }
        ),
        deviceName = "Phone Name",
        wifiSSID = "LocalSpot1231"
    )

    NetworkNodeListScreen(uiStateVal)
}