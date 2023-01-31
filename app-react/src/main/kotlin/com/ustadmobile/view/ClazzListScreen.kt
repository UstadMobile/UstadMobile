package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.ClazzListUiState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.common.lg
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.util.colorForAttendanceStatus
import com.ustadmobile.util.ext.format
import csstype.*
import mui.icons.material.LensRounded
import mui.icons.material.People
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create

external interface ClazzListScreenProps : Props {

    var uiState: ClazzListUiState

    var onClickClazz: (Clazz?) -> Unit

    var onClickSort: () -> Unit

    var onClickFilterChip: (MessageIdOption2?) -> Unit

}

val ClazzListScreenPreview = FC<Props> {

    ClazzListScreenComponent2 {
        uiState = ClazzListUiState(
            clazzList = listOf(
                ClazzWithListDisplayDetails().apply {
                    clazzUid = 1
                    clazzName = "Class Name"
                    clazzDesc = "Class Description"
                    attendanceAverage = 0.3F
                    numTeachers = 3
                    numStudents = 2
                },
                ClazzWithListDisplayDetails().apply {
                    clazzUid = 2
                    clazzName = "Class Name"
                    clazzDesc = "Class Description"
                    attendanceAverage = 0.3F
                    numTeachers = 3
                    numStudents = 2
                },
                ClazzWithListDisplayDetails().apply {
                    clazzUid = 3
                    clazzName = "Class Name"
                    clazzDesc = "Class Description"
                    attendanceAverage = 0.3F
                    numTeachers = 3
                    numStudents = 2
                }
            )
        )
    }
}

private val ClazzListScreenComponent2 = FC<ClazzListScreenProps> { props ->

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(10.px)

            UstadListSortHeader {
                activeSortOrderOption = props.uiState.activeSortOrderOption
                enabled = props.uiState.fieldsEnabled
                onClickSort = props.onClickSort
            }

            UstadListFilterChipsHeader{
                filterOptions = props.uiState.filterOptions
                selectedChipId = props.uiState.selectedChipId
                enabled = props.uiState.fieldsEnabled
                onClickFilterChip = props.onClickFilterChip
            }

            Grid {
                container = true

                props.uiState.clazzList.forEach { clazz ->
                    Grid {
                        item = true
                        xs = 12
                        md = 6
                        lg = 4

                        ClazzListItem {
                            clazzItem = clazz
                            onClickClazz = props.onClickClazz
                        }
                    }
                }
            }
        }
    }
}



external interface ClazzListItemProps : Props {

    var clazzItem: ClazzWithListDisplayDetails

    var onClickClazz: (ClazzWithListDisplayDetails) -> Unit

}

private val ClazzListItem = FC<ClazzListItemProps> { props ->

    val strings = useStringsXml()
    val role = (RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == props.clazzItem.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.messageId ?: MessageID.student)

    Card {
        onClick = { props.onClickClazz(props.clazzItem) }
        sx {
            margin = 10.px
            padding = 15.px
        }

        Stack {
            direction = responsive(StackDirection.column)

            Stack {
                direction = responsive(StackDirection.row)
                justifyContent = JustifyContent.spaceBetween

                Stack {
                    direction = responsive(StackDirection.column)

                    Typography {
                        + (props.clazzItem.clazzName ?: "")
                    }

                    Typography {
                        + (props.clazzItem.clazzDesc ?: "")
                    }
                }

                Stack {
                    direction = responsive(StackDirection.row)

                    + mui.icons.material.Badge.create()

                    + strings[role]
                }

            }

            Stack {
                direction = responsive(StackDirection.row)

                LensRounded {
                    color = colorForAttendanceStatus(props.clazzItem.attendanceAverage)
                    sx {
                        width = 15.px
                        height = 15.px

                        // To align with List Item Button
                        padding = Padding(
                            top = 5.px,
                            bottom = 0.px,
                            right = 0.px,
                            left = 0.px,
                        )
                    }
                }

                Typography {
                    + (props.clazzItem.attendanceAverage * 100)
                        .toString()
                        .format(strings[MessageID.x_percent_attended])
                }

                Box{
                    sx { width = 10.px }
                }

                + People.create()

                Typography {
                    + strings[MessageID.x_teachers_y_students]
                        .replace("%1\$d", props.clazzItem.numTeachers.toString())
                        .replace("%2\$d", props.clazzItem.numStudents.toString())
                }

            }
        }
    }
}