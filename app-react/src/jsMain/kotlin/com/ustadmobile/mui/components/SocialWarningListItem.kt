package com.ustadmobile.mui.components

import mui.icons.material.Circle
import mui.icons.material.Warning
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Divider
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Paper
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.Color
import web.cssom.px

external interface SocialWarningProps : Props {
    var cautions: List<String>
    var onDismiss: () -> Unit
    var onLearnMore: () -> Unit
}

val SocialWarningListItem = FC<SocialWarningProps> { props ->
    Paper {
        sx {
            marginBottom = 16.px
            backgroundColor = Color("rgba(0, 0, 0, 0.02)")
        }

        // Warning header with icon and buttons
        ListItem {
            ListItemIcon {
                Warning {  // Make sure this is imported from mui.icons.material
                    sx {
                        color = Color("#d32f2f")
                    }
                }
            }

            ListItemText {
                primary = ReactNode("Be Careful")
                secondary = ReactNode("Please be mindful when interacting with others")
            }

            // Action buttons
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(1)

                Button {
                    variant = ButtonVariant.text
                    onClick = { props.onDismiss?.invoke() }
                    +"Got it"
                }

                Button {
                    variant = ButtonVariant.text
                    onClick = { props.onLearnMore?.invoke() }
                    +"Learn more"
                }
            }
        }

        Divider()

        // Caution messages list
        List {
            props.cautions.forEach { message ->
                ListItem {
                    ListItemIcon {
                        Circle {  // Make sure this is imported from mui.icons.material
                            sx {
                                color = Color("rgba(0, 0, 0, 0.38)")
                                fontSize = 8.px
                            }
                        }
                    }
                    ListItemText {
                        primary = ReactNode(message)
                    }
                }
            }
        }
    }
}



