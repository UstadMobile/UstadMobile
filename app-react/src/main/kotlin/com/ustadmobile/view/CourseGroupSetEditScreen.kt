package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditUiState
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.DropDownOption
import com.ustadmobile.mui.components.UstadDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.icons.material.AccountCircle
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

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
                onChange = { newString ->
                    props.onCourseGroupSetChange(
                        props.uiState.courseGroupSet?.shallowCopy {
                            cgsTotalGroups = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                        }
                    )
                }
            }

            Button {
                onClick = { props.onClickAssign }
                variant = ButtonVariant.contained

                + strings[MessageID.assign_to_random_groups]
            }

            val groups =  (1 ..(props.uiState.courseGroupSet?.cgsTotalGroups ?: 1)).map {
                DropDownOption("${strings[MessageID.group]} $it", "$it")
            }

            List {
                props.uiState.membersList.forEach { member ->
                    ListItem{
                        sx {
                            paddingTop = 16.px
                            paddingBottom = 16.px
                        }


                        ListItemIcon {
                            AccountCircle()
                        }

                        ListItemText {
                            + (member.name)
                        }

                        ListItemSecondaryAction {

                            sx {
                                width = 150.px
                            }

                            UstadDropDownField {
                                value = groups.firstOrNull { it.value == member.cgm.cgmGroupNumber.toString() }
                                label = ""
                                options = groups
                                itemLabel = { ReactNode((it as? DropDownOption)?.label ?: "") }
                                itemValue = { (it as? DropDownOption)?.value ?: "" }
                                onChange = {
                                    props.onCgmChange(member.cgm.shallowCopy{
                                        cgmGroupNumber = (it as DropDownOption).value.toInt()
                                    })
                                }
                            }
                        }
                    }
                }
            }


        }
    }

}

val CourseGroupSetEditScreenPreview = FC<Props>{
    CourseGroupSetEditComponent2{
        onCgmChange = {

        }
        uiState = CourseGroupSetEditUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "ttl"
                cgsTotalGroups = 6
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    }
}