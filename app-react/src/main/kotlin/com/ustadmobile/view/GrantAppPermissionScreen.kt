package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.GrantAppPermissionUiState
import csstype.TextAlign
import csstype.px
import mui.icons.material.Folder
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props

external interface GrantAppPermissionScreenProps: Props {

    var uiState: GrantAppPermissionUiState

    var onClickGrant: () -> Unit

    var onClickCancel: () -> Unit

}

val GrantAppPermissionScreenComponent2 = FC<GrantAppPermissionScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Folder {
                sx {
                    width = 64.px
                    height = 64.px
                }
            }

            Typography {
                sx {
                    textAlign = TextAlign.center
                }
                + props.uiState.grantToAppName
            }

            Typography {
                + ("This app will receive your profile information and information about your courses")
            }

            Button {
                onClick = { props.onClickGrant() }
                disabled = !props.uiState.fieldsEnabled
                variant = ButtonVariant.contained
                + strings[MessageID.accept].uppercase()
            }


            Button {
                onClick = { props.onClickCancel() }
                disabled = !props.uiState.fieldsEnabled
                variant = ButtonVariant.outlined
                + strings[MessageID.cancel].uppercase()
            }

        }
    }

}

val GrantAppPermissionScreenPreview = FC<Props> {
    GrantAppPermissionScreenComponent2 {
        uiState = GrantAppPermissionUiState(
            grantToAppName = "App Name"
        )
    }
}