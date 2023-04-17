package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.view.components.UstadSwitchField
import csstype.JustifyContent
import csstype.px
import mui.icons.material.Bluetooth
import mui.icons.material.Smartphone
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface NetworkNodeListScreenProps : Props {

    var uiState: NetworkNodeListUiState

    var onChangeBluetoothEnabled: (Boolean) -> Unit

    var onChangeHotspotEnabled: (Boolean) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickNetworkNode: (NetworkNode) -> Unit
}

val NetworkNodeListScreenPreview = FC<Props> {

    NetworkNodeListScreenComponent2 {
        uiState = NetworkNodeListUiState(
            networkNodes = listOf(
                NetworkNode().apply {
                    nodeId = 1
                }
            ),
            deviceName = "Phone Name",
            wifiSSID = "LocalSpot1231"
        )
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
                props.uiState.networkNodes.forEach { networkNode ->

                    ListItem {
                        ListItemButton {
                            onClick = { props.onClickNetworkNode(networkNode) }

                            ListItemIcon {
                                + Stack.create {
                                    direction = responsive(StackDirection.column)
                                    spacing = responsive(10.px)
                                    justifyContent = JustifyContent.center

                                    Bluetooth {
                                        sx {
                                            width = 35.px
                                            height = 35.px
                                        }
                                    }

                                    LinearProgress {
                                        value = 5
                                        variant = LinearProgressVariant.determinate
                                        sx {
                                            width = 45.px
                                        }
                                    }
                                }
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