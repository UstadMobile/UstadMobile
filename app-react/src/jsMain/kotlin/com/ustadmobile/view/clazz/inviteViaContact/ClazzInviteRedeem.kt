package com.ustadmobile.view.clazz.inviteViaContact

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.core.viewmodel.clazz.redeem.InviteRedeemUiState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import react.FC
import react.Props
import web.cssom.TextAlign
import mui.system.sx
import web.cssom.AlignItems
import web.cssom.px


external interface ClazzInviteRedeemProps : Props {
    var uiState: InviteRedeemUiState
    var processDecision: (Boolean) -> Unit
}

val ClazzInviteRedeemScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzInviteViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(InviteRedeemUiState())

    ClazzInviteRedeemComponent2 {
        uiState = uiStateVal
        processDecision = viewModel::processDecision
    }
}

private val ClazzInviteRedeemComponent2 = FC<ClazzInviteRedeemProps> { props ->
    val strings = useStringProvider()
    val uiState = props.uiState




    UstadStandardContainer {
        maxWidth = "lg"

        Typography {
            sx {
                textAlign = TextAlign.center
            }
            +strings[MR.strings.do_you_want_to_join_this_course]
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(10.px)

            sx {
                alignItems = AlignItems.center
            }

            Button {
                onClick = { props.processDecision(true) }
                variant = ButtonVariant.outlined
                +strings[MR.strings.accept].uppercase()
            }



            Button {
                onClick = { props.processDecision(false) }
                variant = ButtonVariant.outlined
                +strings[MR.strings.decline].uppercase()
            }
        }

    }
}
