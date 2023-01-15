package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzLogEditUiState
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateTimeEditField
import csstype.px
import mui.material.Container
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface ClazzLogEditScreenProps : Props {

    var uiState: ClazzLogEditUiState

    var onChangeClazzLog: (ClazzLog?) -> Unit

}


private val ClazzLogEditScreenComponent2 = FC<ClazzLogEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadDateTimeEditField {
                timeInMillis = props.uiState.clazzLog?.logDate ?: 0
                label = strings[MessageID.date]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.dateError
                timeZoneId = props.uiState.timeZone
                onChange = {
                    props.onChangeClazzLog(props.uiState.clazzLog?.shallowCopy {
                        logDate = it
                    })
                }
            }
        }
    }
}

val ClazzLogEditScreenPreview = FC<Props> {


    var clazzLogUiState by useState {
        ClazzLogEditUiState(clazzLog = ClazzLog())
    }

    ClazzLogEditScreenComponent2 {
        uiState = clazzLogUiState
        onChangeClazzLog = {
            clazzLogUiState = clazzLogUiState.copy(
                clazzLog = it
            )
        }
    }
}
