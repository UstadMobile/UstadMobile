package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzLogEditUiState
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadTimeEditField
import csstype.px
import mui.material.Container
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props

external interface ClazzLogEditScreenProps : Props {

    var uiState: ClazzLogEditUiState

    var onChangeDate: (Long) -> Unit

    var onChangeTime: (Int) -> Unit

}

val ClazzLogEditScreenPreview = FC<Props> {

    ClazzLogEditScreenComponent2 {
        uiState = ClazzLogEditUiState()
    }
}

private val ClazzLogEditScreenComponent2 = FC<ClazzLogEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadDateEditField {
                timeInMillis = props.uiState.date
                label = strings[MessageID.date]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.dateError
                timeZoneId = props.uiState.timeZone
                onChange = { props.onChangeDate(it) }
            }

            UstadTimeEditField {
                timeInMillis = props.uiState.time
                label = strings[MessageID.time]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.timeError
                onChange = { props.onChangeTime(it) }
            }
        }
    }
}