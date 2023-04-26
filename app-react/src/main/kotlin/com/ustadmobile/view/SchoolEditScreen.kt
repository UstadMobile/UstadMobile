package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import csstype.px
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import react.useState

external interface SchoolEditScreenProps : Props {

    var uiState: SchoolEditUiState

    var onSchoolChanged: (SchoolWithHolidayCalendar?) -> Unit

    var onClickTimeZone: () -> Unit

    var onClickHolidayCalendar: () -> Unit

}

val SchoolEditComponent2 = FC <SchoolEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(15.px)

            TextField {
                id = "schoolName"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolName ?: ""
                label = ReactNode(strings[MessageID.name])
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolName = currentVal?.toString() ?: ""
                    })
                }
            }

            TextField {
                id = "schoolDesc"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolDesc ?: ""
                label = ReactNode(strings[MessageID.description])
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolDesc = currentVal?.toString() ?: ""
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.schoolTimeZone ?: ""
                label = strings[MessageID.timezone]
                enabled = props.uiState.fieldsEnabled
                readOnly = true
                onClick = props.onClickTimeZone
                onChange = { }
            }

            UstadTextEditField {
                value = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                label = strings[MessageID.holiday_calendar]
                enabled = props.uiState.fieldsEnabled
                readOnly = true
                onClick = props.onClickHolidayCalendar
                onChange = { }
            }

            TextField {
                id = "schoolAddress"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolAddress ?: ""
                label = ReactNode(strings[MessageID.address])
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolAddress = currentVal?.toString() ?: ""
                    })
                }
            }

            TextField {
                id = "schoolPhoneNumber"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolPhoneNumber ?: ""
                label = ReactNode(strings[MessageID.phone_number])
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolPhoneNumber = currentVal?.toString() ?: ""
                    })
                }
            }

            TextField {
                id = "schoolPhoneNumber"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolEmailAddress ?: ""
                label = ReactNode(strings[MessageID.email])
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolEmailAddress = currentVal?.toString() ?: ""
                    })
                }
            }

            Box {
                sx {
                    height = 10.px
                }
            }
        }
    }
}



val SchoolEditScreenPreview = FC<Props> {

    val uiStateVar by useState {
        SchoolEditUiState(
            entity = SchoolWithHolidayCalendar().apply {
                schoolName = "School A"
                schoolDesc = "This is a test school"
                schoolTimeZone = "Asia/Dubai"
                schoolAddress = "123, Main Street, Nairobi, Kenya"
                schoolPhoneNumber = "+90012345678"
                schoolEmailAddress = "info@schoola.com"
            },
        )
    }

    SchoolEditComponent2 {
        uiState = uiStateVar
    }
}
