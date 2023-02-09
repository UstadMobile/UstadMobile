package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SchoolListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import csstype.*
import mui.icons.material.AccountBalance
import mui.icons.material.LocationOn
import mui.icons.material.People
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface SchoolListScreenProps: Props {

    var uiState: SchoolListUiState

    var onClickSchool: (SchoolWithMemberCountAndLocation) -> Unit

}

val SchoolListScreenComponent2 = FC<SchoolListScreenProps> { props ->

    Container{

        val strings = useStringsXml()

        props.uiState.schoolList.forEach { school ->

            val schoolUiState = school.listItemUiState
            val memberCount = strings[MessageID.num_items_with_name_with_comma]
                .replace("%1\$d", school.numStudents.toString())
                .replace("%2\$s", strings[MessageID.students])
                .replace("%3\$d", school.numTeachers.toString())
                .replace("%4\$s", strings[MessageID.teachers_literal])

            ListItem{

                ListItemButton {

                    onClick = {
                        props.onClickSchool(school)
                    }

                    ListItemText {
                        sx {
                            textAlign = TextAlign.end
                        }
                        primary = ReactNode(school.schoolName ?: "")
                        secondary = Stack.create {
                            if (schoolUiState.schoolAddressVisible) {
                                Stack {
                                    direction = responsive(StackDirection.row)
                                    sx {
                                        justifyContent = JustifyContent.end
                                    }

                                    Typography {
                                        + (school.schoolAddress ?: "")
                                    }

                                    Icon {
                                        + LocationOn.create()
                                    }
                                }
                            }

                            Stack {
                                direction = responsive(StackDirection.row)
                                sx {
                                    justifyContent = JustifyContent.end
                                }

                                Typography {
                                    + memberCount
                                }

                                Icon {
                                    + People.create()
                                }
                            }

                        }
                    }
                }

                secondaryAction = AccountBalance.create{
                    sx{
                        width = 80.px
                        height = 80.px
                    }
                }
            }
        }

    }

}

val SchoolListScreenPreview = FC<Props> {
    SchoolListScreenComponent2{
        uiState = SchoolListUiState(
            schoolList = listOf(
                SchoolWithMemberCountAndLocation().apply {
                    schoolName = "School A"
                    schoolAddress = "Nairobi, Kenya"
                    numStudents = 460
                    numTeachers = 30
                }
            )
        )
    }
}