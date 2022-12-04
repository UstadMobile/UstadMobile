package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.RegisterAgeRedirectUiState
import com.ustadmobile.mui.components.UstadDateEditField
import csstype.px
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Typography
import mui.system.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface RegisterAgeRedirectProps : Props {

    var uiState: RegisterAgeRedirectUiState

    var onSetDate: (Long) -> Unit

    var onClickNext: () -> Unit

}

val RegisterAgeRedirectPreview = FC<Props> {
    val uiStateVal by useState {
        RegisterAgeRedirectUiState()
    }
    RegisterAgeRedirectComponent2 {
        uiState = uiStateVal
    }
}

val RegisterAgeRedirectComponent2 = FC<RegisterAgeRedirectProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(15.px)

            Typography {
                + strings[MessageID.what_is_your_date_of_birth]
            }

            UstadDateEditField {
                timeInMillis = props.uiState.dateOfBirth
                timeZoneId = UstadMobileConstants.UTC
                label = strings[MessageID.birthday]
                onChange = { props.onSetDate(it) }
            }

            Button {
                variant = ButtonVariant.contained
                onClick = { props.onClickNext }

                + strings[MessageID.next].uppercase()
            }
        }
    }
}