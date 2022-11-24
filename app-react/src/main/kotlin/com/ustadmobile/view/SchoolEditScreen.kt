package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.viewmodel.SchoolEditUiState
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.*
import csstype.AlignContent
import csstype.AlignItems
import csstype.TextAlign
import csstype.px
import mui.icons.material.*
import mui.material.Button
import mui.material.ListItem
import mui.material.Typography
import mui.material.TypographyAlign
import mui.system.*
import react.FC
import react.Props
import react.create
import react.useState

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
        Stack {
            spacing = responsive(2)

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
//                readOnly = true
//                onClick = props.onClickTimeZone
                onChange = { }
            }

            UstadTextEditField {
                value = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                label = strings[MessageID.holiday_calendar]
                enabled = props.uiState.fieldsEnabled
//                readOnly = true
//                onClick = props.onClickHolidayCalendar
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

            Typography{
                + strings[MessageID.permissions]
            }

            AddPersonOrGroupButton{
                onClickNew = props.onClickNew
            }

            ScopedGrantsOneToNList {
                uiState = props.uiState
                onClickEditScopedGrant = props.onClickEditScopedGrant
                onClickDeleteScopedGrant = props.onClickDeleteScopedGrant
            }
        }
    }
}

private val AddPersonOrGroupButton = FC<SchoolEditScreenProps> { props ->

    val strings = useStringsXml()

    Button {
        onClick = { props.onClickNew }
        sx {
           textAlign = TextAlign.start
        }

        + Add.create()

        + strings[MessageID.add_person_or_group]
    }
}

private val ScopedGrantsOneToNList = FC<SchoolEditScreenProps> { props ->
    List{
        props.uiState.scopedGrants.forEach {
            ListItem{
                UstadDetailField {
                    valueText = it.name ?: ""
                    labelText = (it.scopedGrant?.sgPermissions ?: 0).toString()
                    onClick = { props.onClickEditScopedGrant(it) }

//                    secondaryActionContent = {
//                        IconButton(
//                            onClick = { onClickDeleteScopedGrant(scopedGrant) },
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.Delete,
//                                contentDescription = stringResource(id = R.string.delete),
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

val SchoolEditScreenPreview = FC<Props> {

    var uiStateVar by useState {
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
