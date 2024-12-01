package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.socialwarning.ShowSocialWarningUseCase.Companion.SOCIAL_WARNING_WEB_URL
import com.ustadmobile.core.hooks.useStringProvider
import js.objects.jso
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.px
import web.window.WindowTarget
import web.window.window
import mui.icons.material.Warning as WarningIcon
private const val BUTTON_STACK_GAP_PX = 12
private const val CONTENT_MARGIN_TOP_PX = 12

external interface SocialWarningProps : Props {
    var onDismiss: () -> Unit
}

val SocialWarningListItem = FC<SocialWarningProps> { props ->
    val strings = useStringProvider()

    ListItem {
        ListItemIcon {
            WarningIcon()
        }

        ListItemText {
            primary = ReactNode(strings[MR.strings.be_careful_interacting_online])
            secondary = Stack.create {
                direction = responsive(StackDirection.column)

                div {
                    + strings[MR.strings.be_careful_not_to_share]
                }

                Stack {
                    direction = responsive(StackDirection.row)
                    sx = jso {
                        gap = BUTTON_STACK_GAP_PX.px
                        marginTop = CONTENT_MARGIN_TOP_PX.px
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
                                SOCIAL_WARNING_WEB_URL,
                                WindowTarget._blank
                            )
                        }
                        +strings[MR.strings.learn_more]
                    }
                }
            }

            secondaryTypographyProps = jso {
                component = div
            }
        }
    }
}