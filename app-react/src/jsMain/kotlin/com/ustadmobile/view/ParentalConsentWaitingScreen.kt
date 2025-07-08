package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentConsentWaitingScreenViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import mui.material.Typography
import web.cssom.px
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface ParentalConsentWaitingScreenProps : Props

val ParentalConsentWaitingScreenComponent2 = FC<ParentalConsentWaitingScreenProps> { props ->


    UstadStandardContainer {

        val strings: StringProvider = useStringProvider()

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            Typography {
                +  ReactNode(strings[MR.strings.wait_for_parent_to_consent])
            }

        }
    }
}

val ParentalConsentWaitingScreenScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ParentConsentWaitingScreenViewModel(di, savedStateHandle)
    }


    ParentalConsentWaitingScreenComponent2 {
    }

}




