package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
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
import web.cssom.px

external interface SocialWarningProps : Props {
    var cautions: List<String>
    var onDismiss: () -> Unit
    var onLearnMore: () -> Unit
}

val SocialWarningListItem = FC<SocialWarningProps> { props ->

    val strings = useStringProvider()

    Paper {
        sx {
            marginBottom = 2.px  // spacing unit as it's about layout, not theming
        }

        // Warning header with icon and buttons
        ListItem {
            ListItemIcon {
                Warning()  // Using default theme color
            }

            ListItemText {
                primary = ReactNode(strings[MR.strings.be_careful],
                )
            }

            // Action buttons
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(1)

                Button {
                    variant = ButtonVariant.text
                    onClick = { props.onDismiss?.invoke() }
                    + strings[MR.strings.got_it]
                }

                Button {
                    variant = ButtonVariant.text
                    onClick = { props.onLearnMore?.invoke() }
                    + strings[MR.strings.learn_more]
                }
            }
        }

        Divider()

        // Caution messages list
        List {
            props.cautions.forEach { message ->
                ListItem {
                    ListItemIcon {
                        Circle()  // Using default theme sizing and color
                    }
                    ListItemText {
                        primary = ReactNode(message)
                    }
                }
            }
        }
    }
}


