package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import js.objects.jso
import mui.material.Box
import mui.icons.material.Warning as WarningIcon
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.JustifyContent
import web.cssom.px
import web.window.WindowTarget
import web.window.window

external interface SocialWarningProps : Props {
    var onDismiss: () -> Unit
}

val SocialWarningListItem = FC<SocialWarningProps> { props ->

    val strings = useStringProvider()

    ListItem {
        ListItemIcon {
            WarningIcon()
        }

        Box {
            sx = jso {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.start
            }

            ListItemText {
                primary = ReactNode(strings[MR.strings.be_careful_interacting_online])
                secondary = ReactNode(strings[MR.strings.be_careful_not_to_share])
            }

            Box {
                sx = jso {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    gap = 12.px
                    marginTop = 12.px
                }

                Button {
                    variant = ButtonVariant.text
                    onClick = { props.onDismiss() }
                    +strings[MR.strings.got_it]
                }

                Button {
                    variant = ButtonVariant.text
                    onClick = {
                        window.open(
                            "https://beinternetawesome.withgoogle.com",
                            WindowTarget._blank
                        )
                    }
                    +strings[MR.strings.learn_more]
                }
            }
        }
    }
}
