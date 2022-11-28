package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.*
import mui.material.List
import mui.material.Box
import mui.icons.material.*
import mui.material.*
import mui.material.Container
import mui.system.*
import mui.material.Stack
import mui.material.styles.TypographyVariant
import react.*

external interface SchoolEditScreenProps : Props {

    var uiState: SchoolEditUiState

    var onSchoolChanged: (SchoolWithHolidayCalendar?) -> Unit

    var onClickTimeZone: () -> Unit

    var onClickHolidayCalendar: () -> Unit

    var onClickNew: () -> Unit

    var onClickEditScopedGrant: (ScopedGrantAndName?) -> Unit

    var onClickDeleteScopedGrant: (ScopedGrantAndName?) -> Unit

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
                label = strings[MessageID.description]
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

            Typography{
                + strings[MessageID.permissions]
                variant = TypographyVariant.h6
            }

            Button {
                onClick = { props.onClickNew }
                variant = ButtonVariant.text
                startIcon = Add.create()

                + strings[MessageID.add_person_or_group]
            }

            ScopedGrantsOneToNList{
                uiState = props.uiState
                onClickEditScopedGrant = props.onClickEditScopedGrant
                onClickDeleteScopedGrant = props.onClickDeleteScopedGrant
            }
        }
    }
}

private val ScopedGrantsOneToNList = FC<SchoolEditScreenProps> { props ->
    List{
        props.uiState.scopedGrants.forEach { scopedGrant ->
            ListItem{
                UstadDetailField {
                    valueText = scopedGrant.name ?: ""
                    labelText = (scopedGrant.scopedGrant?.sgPermissions ?: 0).toString()
                    onClick = { props.onClickEditScopedGrant(scopedGrant) }

                    secondaryActionContent = IconButton.create {
                        onClick = { props.onClickDeleteScopedGrant(scopedGrant) }
                        Delete {}
                    }
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
            scopedGrants = listOf(
                ScopedGrantAndName().apply {
                    name = "Person Name"
                },
                ScopedGrantAndName().apply {
                    name = "Person Name"
                },
                ScopedGrantAndName().apply {
                    name = "Person Name"
                }
            )
        )
    }

    SchoolEditComponent2 {
        uiState = uiStateVar
    }
}
