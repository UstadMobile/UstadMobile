package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItem
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.Person
import csstype.px
import csstype.rgba
import js.core.jso
import mui.icons.material.EmojiEvents
import mui.icons.material.Person2
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.span

external interface UstadCourseAssignmentMarkListItemProps : Props {

    var uiState: UstadCourseAssignmentMarkListItem

    var onClickMark: (CourseAssignmentMarkWithPersonMarker) -> Unit

}

val UstadCourseAssignmentMarkListItem = FC<UstadCourseAssignmentMarkListItemProps> { props ->

    val strings = useStringsXml()

    var text = props.uiState.mark.marker?.fullName() ?: ""

    if (props.uiState.markerGroupNameVisible){
        text += "  (${strings[MessageID.group_number]
            .replace("%1\$s", props.uiState.mark.camMarkerSubmitterUid.toString())})"
    }

    val formattedTime = useFormattedTime(props.uiState.mark.camLct.toInt())

    ListItem{
        ListItemButton {
            onClick = {
                props.onClickMark(props.uiState.mark)
            }

            ListItemIcon {
                + Person2.create {
                    sx {
                        width = 40.px
                        height = 40.px
                    }
                }
            }

            ListItemText {
                primary = ReactNode(text)
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    Stack {
                        direction = responsive(StackDirection.row)

                        Icon {
                            + EmojiEvents.create()
                        }

                        Typography {
                            + ("${props.uiState.mark.camMark}/${props.uiState.block.cbMaxPoints}" +
                                    " ${strings[MessageID.points]}")

                            + " "

                            if (props.uiState.camPenaltyVisible) {
                                span { style = jso {
                                    color = rgba(255, 0,0, 1.0)
                                }
                                    child(ReactNode(
                                        strings[MessageID.late_penalty]
                                            .replace("%1\$s", (
                                                    props.uiState.block.cbLateSubmissionPenalty)
                                                .toString())
                                    ))
                                }
                            }
                        }
                    }


                    Typography {
                        + (props.uiState.mark.camMarkerComment ?: "")
                    }
                }
            }
        }
        secondaryAction = Typography.create {
            + formattedTime
        }
    }
}

val UstadMarksPersonListItemPreview = FC<Props> {

    UstadCourseAssignmentMarkListItem {
        uiState = UstadCourseAssignmentMarkListItem(
            mark = CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3
                }
            }
        )
    }
}