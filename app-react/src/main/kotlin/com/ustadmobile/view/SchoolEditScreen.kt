package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.inputCursor
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.util.ext.onTextChange
import csstype.Cursor
import csstype.px
import js.core.jso
import mui.material.Container
import mui.material.TextField
import mui.material.FormControlVariant
import mui.material.Stack
import mui.material.Box
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
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
                onTextChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolName = it
                    })
                }
            }

            TextField {
                id = "schoolDesc"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolDesc ?: ""
                label = ReactNode(strings[MessageID.description])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolDesc = it
                    })
                }
            }

            TextField {
                sx {
                    inputCursor = Cursor.pointer
                }
                id = "schoolTimeZone"
                value = props.uiState.entity?.schoolTimeZone ?: ""
                label = ReactNode(strings[MessageID.timezone])
                disabled = !props.uiState.fieldsEnabled
                onClick = { props.onClickTimeZone() }
                inputProps = jso {
                    readOnly = true
                }
            }

            TextField {
                sx {
                    inputCursor = Cursor.pointer
                }
                id = "umCalendarName"
                value = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                label = ReactNode(strings[MessageID.holiday_calendar])
                disabled = !props.uiState.fieldsEnabled
                onClick = { props.onClickHolidayCalendar() }
                inputProps = jso {
                    readOnly = true
                }
            }

            TextField {
                id = "schoolAddress"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolAddress ?: ""
                label = ReactNode(strings[MessageID.address])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolAddress = it
                    })
                }
            }

            TextField {
                id = "schoolPhoneNumber"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolPhoneNumber ?: ""
                label = ReactNode(strings[MessageID.phone_number])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onSchoolChanged(props.uiState.entity?.shallowCopy {
                        schoolPhoneNumber = it
                    })
                }
            }

            TextField {
                id = "schoolEmailAddress"
                variant = FormControlVariant.outlined
                value = props.uiState.entity?.schoolEmailAddress ?: ""
                label = ReactNode(strings[MessageID.email])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
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
