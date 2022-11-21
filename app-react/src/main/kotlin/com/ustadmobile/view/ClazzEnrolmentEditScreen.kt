package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.ClazzEnrolmentEditUiState
import csstype.px
import mui.icons.material.People
import mui.material.Container
import mui.material.Typography
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.create
import react.useState

external interface ClazzEnrolmentEditScreenProps : Props {

    var uiState: ClazzEnrolmentEditUiState

}

val ClazzEnrolmentEditScreenComponent2 = FC<ClazzEnrolmentEditScreenProps> { props ->

    Container {
        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(10.px)

            + People.create()

            Stack {
                direction = responsive(StackDirection.column)
                spacing = responsive(5.px)

                Typography {
                    + ""
                }

                Typography{
                    + ""
                }
            }
        }
    }
}

val ClazzEnrolmentEditScreenPreview = FC<Props> {

    val uiStateVar : ClazzEnrolmentEditUiState by useState {
        ClazzEnrolmentEditUiState(
        )
    }

    ClazzEnrolmentEditScreenComponent2 {
        uiState = uiStateVar
    }
}