package com.ustadmobile.view.person.registerageredirect

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectUiState
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.mui.components.UstadStandardContainer
import web.cssom.px
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Typography
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface RegisterAgeRedirectProps : Props {

    var uiState: RegisterAgeRedirectUiState

    var onSetDate: (Long) -> Unit

    var onClickNext: () -> Unit

}

val RegisterAgeRedirectScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        RegisterAgeRedirectViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(RegisterAgeRedirectUiState())

    RegisterAgeRedirectComponent2 {
        uiState = uiStateVal
        onSetDate = viewModel::onSetDate
        onClickNext = viewModel::onClickNext
    }

}

val RegisterAgeRedirectComponent2 = FC<RegisterAgeRedirectProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(15.px)

            Typography {
                + strings[MR.strings.what_is_your_date_of_birth]
            }

            UstadDateField {
                id = "age_date_of_birth"
                timeInMillis = props.uiState.dateOfBirth
                timeZoneId = UstadMobileConstants.UTC
                label = ReactNode(strings[MR.strings.birthday] + "*")
                onChange = { props.onSetDate(it) }
                error = props.uiState.dateOfBirthError != null
                helperText = ReactNode(props.uiState.dateOfBirthError ?: strings[MR.strings.required])
            }

        }
    }
}