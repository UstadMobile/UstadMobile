package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.CourseGroupSetEditUiState
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.DropDownOption
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.px
import mui.icons.material.AccountBalanceRounded
import mui.icons.material.AccountCircle
import mui.material.*
import mui.system.responsive
import mui.system.sx
import popper.core.Modifier
import react.*

external interface CourseGroupSetEditProps: Props{
    var uiState: CourseGroupSetEditUiState
    var onCourseGroupSetChange: (CourseGroupSet?) -> Unit
    var onClickAssign: () -> Unit
    var onCgmChange: (CourseGroupMember?) -> Unit
}

val CourseGroupSetEditComponent2 = FC<CourseGroupSetEditProps> { props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(26.px)

            UstadTextEditField {
                value = props.uiState.courseGroupSet?.cgsName ?: ""
                label = strings[MessageID.title]
                error = props.uiState.courseTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseGroupSetChange(
                        props.uiState.courseGroupSet?.shallowCopy {
                            cgsName = it
                        }
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.courseGroupSet?.cgsTotalGroups.toString()
                label = strings[MessageID.number_of_groups]
                error = props.uiState.numOfGroupsError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseGroupSetChange(
                        props.uiState.courseGroupSet?.shallowCopy {
                            cgsTotalGroups = it.toInt()
                        }
                    )
                }
            }

            Button {
                onClick = { props.onClickAssign }
                variant = ButtonVariant.contained

                + strings[MessageID.assign_to_random_groups]
            }

            val groups =  (1 ..(props.uiState.courseGroupSet?.cgsTotalGroups ?: 1)).map { DropDownOption("Group $it", "$it") }

            props.uiState.membersList.forEach { member ->
                ListItem{
                    disablePadding = true

                    ListItemSecondaryAction {

                        sx {
                            width = 150.px
                        }

                        var selectedOption: DropDownOption? by useState { DropDownOption("Group ${member.cgm.cgmGroupNumber}", "${member.cgm.cgmGroupNumber}") }

                        UstadDropDownField {
                            value = selectedOption
                            label = ""
                            options = groups
                            itemLabel = { ReactNode((it as? DropDownOption)?.label ?: "") }
                            itemValue = { (it as? DropDownOption)?.value ?: "" }
                            onChange = {
                                selectedOption = it as? DropDownOption
                                props.onCgmChange(member.cgm.shallowCopy{
                                    cgmGroupNumber = (it as DropDownOption).value as Int
                                })
                            }
                        }
                    }

                    ListItemIcon {
                        AccountCircle()
                    }

                    ListItemText {
                        + (member.name)
                    }

                }
            }

        }
    }

}

val CourseGroupSetEditScreenPreview = FC<Props>{
    CourseGroupSetEditComponent2{
        uiState = CourseGroupSetEditUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "ttl"
                cgsTotalGroups = 6
            },
            membersList = listOf(
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                com.ustadmobile.core.viewmodel.CourseGroupMemberPerson(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    }
}