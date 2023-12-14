package com.ustadmobile.view.coursegroupset.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditUiState
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.appendGroupNumIfNotInList
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadSelectField
import web.cssom.px
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
    var onClickAssignRandomly: () -> Unit
    var onChangeGroupAssignment: (personUid: Long, groupNumber: Int) -> Unit
}

val CourseGroupSetEditComponent2 = FC<CourseGroupSetEditProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(26.px)

            UstadTextField {
                id = "cgs_name"
                value = props.uiState.courseGroupSet?.cgsName ?: ""
                label = ReactNode(strings[MR.strings.title])
                error = props.uiState.courseTitleError != null
                helperText = props.uiState.courseTitleError?.let { ReactNode(it) }
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onCourseGroupSetChange(
                        props.uiState.courseGroupSet?.shallowCopy {
                            cgsName = it
                        }
                    )
                }
            }

            UstadNumberTextField {
                id = "cgs_total_groups"
                numValue = (props.uiState.courseGroupSet?.cgsTotalGroups ?: 2).toFloat()
                label = ReactNode(strings[MR.strings.number_of_groups])
                helperText = props.uiState.numOfGroupsError?.let { ReactNode(it) }
                error = props.uiState.numOfGroupsError != null
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseGroupSetChange(
                        props.uiState.courseGroupSet?.shallowCopy {
                            cgsTotalGroups = it.toInt()
                        }
                    )
                }
            }

            Button {
                id = "assign_random_groups"
                onClick = { props.onClickAssignRandomly() }
                variant = ButtonVariant.contained

                + strings[MR.strings.assign_to_random_groups]
            }

            val groupOptions = (0..(props.uiState.courseGroupSet?.cgsTotalGroups ?: 1)).toList()
            val itemLabelFn: (Int) -> ReactNode = {
                ReactNode(
                    if(it == 0) strings[MR.strings.unassigned] else "${strings[MR.strings.group]} $it"
                )
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

                            val assignedGroupNum = member.cgm?.cgmGroupNumber ?: 0

                            UstadSelectField<Int> {
                                id = "person_${member.personUid}_groupselect"
                                label = ""
                                value = member.cgm?.cgmGroupNumber ?: 0
                                options = groupOptions.appendGroupNumIfNotInList(assignedGroupNum)
                                itemValue = { it.toString() }
                                itemLabel = itemLabelFn
                                onChange = {
                                    props.onChangeGroupAssignment(member.personUid, it)
                                }
                                fullWidth = false
                                enabled = props.uiState.fieldsEnabled
                                error = (assignedGroupNum !in groupOptions)
                            }
                        }
                    }
                }
            }
        }
    }

}


val CourseGroupSetEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseGroupSetEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CourseGroupSetEditUiState())


    CourseGroupSetEditComponent2 {
        uiState = uiStateVal
        onChangeGroupAssignment = viewModel::onChangeGroupAssignment
        onCourseGroupSetChange = viewModel::onEntityChanged
        onClickAssignRandomly = viewModel::onClickAssignRandomly
    }

}
