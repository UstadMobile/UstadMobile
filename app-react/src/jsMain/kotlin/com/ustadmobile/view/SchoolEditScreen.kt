package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import web.cssom.px
import mui.material.Box
import mui.material.Container
import mui.material.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
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

            UstadTextEditField {
                value = props.uiState.entity?.schoolName ?: ""
                label = strings[MessageID.name]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolName = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.schoolDesc ?: ""
                label = strings[MessageID.description]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolDesc = it
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

            UstadTextEditField {
                value = props.uiState.entity?.schoolAddress ?: ""
                label = strings[MessageID.address]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolAddress = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.schoolPhoneNumber ?: ""
                label = strings[MessageID.phone_number]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolPhoneNumber = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.schoolEmailAddress ?: ""
                label = strings[MessageID.email]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolEmailAddress = it
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
