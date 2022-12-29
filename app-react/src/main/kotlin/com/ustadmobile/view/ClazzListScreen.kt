package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.viewmodel.ClazzListUiState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.util.ext.format
import csstype.JustifyContent
import csstype.px
import mui.icons.material.LensRounded
import mui.icons.material.People
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface ClazzListScreenProps : Props {

    var uiState: ClazzListUiState

    var onClickClazz: (Clazz?) -> Unit

}

val ClazzListScreenPreview = FC<Props> {
    val strings = useStringsXml()
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
                }
            )
        )
    }
}

private val ClazzListScreenComponent2 = FC<ClazzListScreenProps> { props ->

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)
            List{
                props.uiState.clazzList.forEach { clazz ->
                    ClazzListItem {
                        clazzItem = clazz
                        onClickClazz = props.onClickClazz
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

    ListItem{
        ListItemButton {
            onClick = {
                props.onClickClazz(props.clazzItem)
            }

            ListItemText {
                primary = ReactNode(props.clazzItem.clazzName ?: "")
                secondary = Stack.create {

                    Typography {
                        + (props.clazzItem.clazzDesc ?: "")
                    }

                    Stack {
                        direction = responsive(StackDirection.row)

                        + LensRounded.create()

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
                            + (strings[MessageID.x_teachers_y_students]
                                .replace("%1\$d", props.clazzItem.numTeachers)
                                .replace("%2\$s", props.clazzItem.numStudents) ?: "")
                        }
                    }
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(5.px)
                justifyContent = JustifyContent.center

                + mui.icons.material.Badge.create()

                + strings[role]
            }
        }
    }
}