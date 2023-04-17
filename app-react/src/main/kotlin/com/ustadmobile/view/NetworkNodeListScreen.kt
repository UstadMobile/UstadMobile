package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.lib.db.entities.DeviceSession
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import mui.icons.material.Smartphone
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface NetworkNodeListScreenProps : Props {

    var uiState: NetworkNodeListUiState

    var onChangeBluetoothEnabled: (Boolean) -> Unit

    var onChangeHotspotEnabled: (Boolean) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickDevice: (DeviceSession) -> Unit
}

val NetworkNodeListScreenPreview = FC<Props> {
    NetworkNodeListScreenComponent2 {

    }
}

val NetworkNodeListScreenComponent2 = FC<NetworkNodeListScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                valueText = ReactNode(props.uiState.deviceName)
                labelText = strings[MessageID.device]
                icon = Smartphone.create()
            }

            UstadSwitchField {
                checked = props.uiState.fieldsEnabled
                label = strings[MessageID.bluetooth_sharing]
                enabled = props.uiState.fieldsEnabled
                onChanged = {
                    props.onChangeBluetoothEnabled(it)
                }
            }

            UstadSwitchField {
                checked = props.uiState.fieldsEnabled
                label = strings[MessageID.hotspot_sharing]
                enabled = props.uiState.fieldsEnabled
                onChanged = {
                    props.onChangeHotspotEnabled(it)
                }
            }

            Typography {
                + "${strings[MessageID.wifi_ssid]}: ${props.uiState.wifiSSID}"
            }

            Divider()

            UstadListFilterChipsHeader {
                filterOptions = props.uiState.deviceFilterOptions
                selectedChipId = props.uiState.selectedChipId
                enabled = props.uiState.fieldsEnabled
                onClickFilterChip = props.onClickFilterChip
            }


            List{
                props.uiState.devices.forEach { device ->

                    ListItem {
                        ListItemButton {
                            onClick = { props.onClickDevice(device) }

                            ListItemIcon {

                            }
                            ListItemText {
                                primary = ReactNode("Phone Number")
                                secondary = ReactNode("Server")
                            }
                        }
                    }
                }
            }
        }
    }
}