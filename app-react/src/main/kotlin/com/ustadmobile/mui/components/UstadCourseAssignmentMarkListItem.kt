package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.ext.penaltyPercentage
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.Person
import csstype.px
import csstype.rgba
import js.core.jso
import mui.icons.material.EmojiEvents
import mui.icons.material.Person2
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.span
import kotlin.math.roundToInt

external interface UstadCourseAssignmentMarkListItemProps : Props {

    var uiState: UstadCourseAssignmentMarkListItemUiState

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

            Stack {
                Typography {
                    variant = TypographyVariant.body1
                    + text
                }

                Stack {
                    direction = responsive(StackDirection.row)

                    Icon {
                        color = IconColor.action
                        fontSize = IconSize.small

                        + EmojiEvents.create()
                    }

                    Typography {
                        variant = TypographyVariant.caption
                        + "${props.uiState.mark.camMark}/${props.uiState.mark.camMaxMark}"
                        + " ${strings[MessageID.points]}"
                        + " "

                        if (props.uiState.camPenaltyVisible) {
                            span {
                                style = jso {
                                    color = rgba(255, 0,0, 1.0)
                                }

                                +strings[MessageID.late_penalty]
                                    .replace(
                                        oldValue = "%1\$s",
                                        newValue = props.uiState.mark.penaltyPercentage().toString()
                                    )
                            }
                        }
                    }
                }

                Typography {
                    variant = TypographyVariant.caption
                    + (props.uiState.mark.camMarkerComment ?: "")
                }
            }
        }
        secondaryAction = Typography.create {
            + formattedTime
        }
    }
}

val UstadCourseAssignmentMarkListItemPreview = FC<Props> {

    UstadCourseAssignmentMarkListItem {
        uiState = UstadCourseAssignmentMarkListItemUiState(
            mark = CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3f
                }
            }
        )
    }
}