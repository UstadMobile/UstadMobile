package com.ustadmobile.view.schedule.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditUiState
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.mui.components.UstadTimeField
import web.cssom.*
import mui.material.*
import mui.system.Container
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
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

            UstadMessageIdSelectField {
                value = props.uiState.entity?.scheduleDay ?: 0
                options = ScheduleConstants.DAY_MESSAGE_IDS
                label = strings[MessageID.day]
                enabled = props.uiState.fieldsEnabled
                id = "day_field"
                onChange = {
                    props.onScheduleChanged(
                        props.uiState.entity?.shallowCopy {
                            scheduleDay = it.value
                        })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)
                justifyContent = JustifyContent.spaceBetween

                UstadTimeField {
                    timeInMillis = (props.uiState.entity?.sceduleStartTime ?: 0).toInt()
                    label = ReactNode(strings[MessageID.from])
                    helperText = props.uiState.fromTimeError?.let { ReactNode(it) }
                    disabled = !props.uiState.fieldsEnabled
                    error = props.uiState.fromTimeError != null
                    fullWidth = true
                    id = "from_time"
                    onChange = {
                        props.onScheduleChanged(
                            props.uiState.entity?.shallowCopy {
                                sceduleStartTime = it.toLong()
                            })
                    }
                }

                UstadTimeField {
                    timeInMillis = (props.uiState.entity?.scheduleEndTime ?: 0).toInt()
                    label = ReactNode(strings[MessageID.to])
                    helperText = props.uiState.toTimeError?.let { ReactNode(it) }
                    disabled = !props.uiState.fieldsEnabled
                    error = props.uiState.toTimeError != null
                    fullWidth = true
                    id = "to_time"
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


val ScheduleEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ScheduleEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(ScheduleEditUiState())

    ScheduleEditComponent2 {
        uiState = uiStateVar
        onScheduleChanged = viewModel::onEntityChanged
    }
}
