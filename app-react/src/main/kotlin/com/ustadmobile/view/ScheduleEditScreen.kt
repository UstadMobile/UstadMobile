package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.DayConstants
import com.ustadmobile.core.viewmodel.ScheduleEditUiState
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateEditField
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTimeEditField
import csstype.*
import io.ktor.websocket.*
import mui.material.*
import mui.system.Container
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.useState

external interface ScheduleEditScreenProps : Props{
    var uiState: ScheduleEditUiState

    var onScheduleChanged: (Schedule?) -> Unit
}

val ScheduleEditComponent2 = FC <ScheduleEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(2)

            UstadMessageIdDropDownField {
                value = props.uiState.entity?.scheduleDay ?: 0
                options = DayConstants.DAY_MESSAGE_IDS
                label = strings[MessageID.day]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onScheduleChanged(
                        props.uiState.entity?.shallowCopy {
                            scheduleDay = it?.value ?: 0
                        })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadTimeEditField {
                    timeInMillis = (props.uiState.entity?.sceduleStartTime ?: 0).toInt()
                    label = strings[MessageID.from]
                    error = props.uiState.fromTimeError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onScheduleChanged(
                            props.uiState.entity?.shallowCopy {
                                sceduleStartTime = it.toLong()
                            })
                    }
                }

                UstadTimeEditField {
                    timeInMillis = (props.uiState.entity?.scheduleEndTime ?: 0).toInt()
                    label = strings[MessageID.to]
                    error = props.uiState.toTimeError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onScheduleChanged(
                            props.uiState.entity?.shallowCopy {
                                scheduleEndTime = it.toLong()
                            }
                        )
                    }
                }
            }
        }
    }
}

val ScheduleEditScreenPreview = FC<Props> {

    var uiStateVar by useState {
        ScheduleEditUiState(
            entity = Schedule().apply {
                scheduleDay = 0
                sceduleStartTime = 45
                scheduleEndTime = 78
            }
        )
    }

    ScheduleEditComponent2 {
        uiState = uiStateVar
        onScheduleChanged = {
            uiStateVar = uiStateVar.copy(entity = it)
        }
    }
}