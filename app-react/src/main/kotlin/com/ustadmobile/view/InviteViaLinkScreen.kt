package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState
import com.ustadmobile.util.ext.format
import csstype.px
import mui.icons.material.*
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
    val strings = useStringsXml()
    InviteViaLinkComponent2 {
        uiState = InviteViaLinkUiState(
            entityName = strings[MessageID.invite_link_desc],
            inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
        )
    }
}

private val InviteViaLinkComponent2 = FC<InviteViaLinkProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            Typography {
                + (strings[MessageID.invite_link_desc])
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

                + strings[MessageID.copy_link].uppercase()
            }

            Box{
                sx {
                    height = 20.px
                }
            }
        }
    }
}