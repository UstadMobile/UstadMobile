package com.ustadmobile.view.clazz.invitevialink

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.invitevialink.InviteViaLinkUiState
import com.ustadmobile.core.viewmodel.clazz.invitevialink.InviteViaLinkViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import web.cssom.px
import mui.icons.material.Attachment as AttachmentIcon
import mui.icons.material.ContentCopy as ContentCopyIcon
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
}

val InviteViaLinkPreview = FC<Props> {
    InviteViaLinkComponent2 {
        uiState = InviteViaLinkUiState(
            inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
        )
    }
}

val InviteViaLinkScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        InviteViaLinkViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(InviteViaLinkUiState())

    InviteViaLinkComponent2 {
        uiState = uiStateVal
        onClickCopyLink = viewModel::onClickCopy
    }
}

private val InviteViaLinkComponent2 = FC<InviteViaLinkProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        maxWidth = "lg"

        Stack {
            Typography {
                + strings[MR.strings.invite_link_desc]
            }

            Box{
                sx {
                    height = 20.px
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(16.px)

                AttachmentIcon()

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
                onClick = { props.onClickCopyLink() }
                variant = ButtonVariant.outlined

                startIcon = ContentCopyIcon.create()

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