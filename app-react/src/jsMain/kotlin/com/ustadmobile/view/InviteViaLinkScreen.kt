package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState
import com.ustadmobile.util.ext.format
import web.cssom.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Attachment
import mui.icons.material.ContentCopy
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create

external interface InviteViaLinkProps : Props {
    var uiState: InviteViaLinkUiState
    var onClickCopyLink: () -> Unit
    var onClickShareLink: () -> Unit
}

val InviteViaLinkPreview = FC<Props> {
    val strings = useStringProvider()
    InviteViaLinkComponent2 {
        uiState = InviteViaLinkUiState(
            entityName = strings[MR.strings.invite_link_desc],
            inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
        )
    }
}

private val InviteViaLinkComponent2 = FC<InviteViaLinkProps> { props ->

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            Typography {
                + (strings[MR.strings.invite_link_desc])
                    .format(props.uiState.entityName)
            }

            Box{
                sx {
                    height = 20.px
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(16.px)

                + Attachment.create()

                Typography {
                    + (props.uiState.inviteLink ?: "")
                }
            }

            Divider { orientation = Orientation.horizontal }

            Box{
                sx {
                    height = 20.px
                }
            }


            Button {
                onClick = { props.onClickCopyLink }
                variant = ButtonVariant.outlined

                startIcon = ContentCopy.create()

                + strings[MR.strings.copy_link].uppercase()
            }

            Box{
                sx {
                    height = 20.px
                }
            }
        }
    }
}