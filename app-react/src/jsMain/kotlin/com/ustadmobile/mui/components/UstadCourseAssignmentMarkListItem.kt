package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.penaltyPercentage
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.view.components.UstadPersonAvatar
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import web.cssom.rgb
import js.core.jso
import kotlinx.datetime.TimeZone
import mui.icons.material.EmojiEvents as EmojiEventsIcon
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.span
import react.useRequiredContext

external interface UstadCourseAssignmentMarkListItemProps : Props {

    var uiState: UstadCourseAssignmentMarkListItemUiState

}

val UstadCourseAssignmentMarkListItem = FC<UstadCourseAssignmentMarkListItemProps> { props ->

    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    var text = props.uiState.markerName

    if (props.uiState.markerGroupNameVisible){
        text += " (${strings.format(MR.strings.group_number, props.uiState.peerGroupNumber.toString())})"
    }

    val formattedTime = useFormattedDateAndTime(
        timeInMillis = props.uiState.mark.courseAssignmentMark?.camLct ?: 0,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem{
        ListItemIcon {
            UstadPersonAvatar {
                personName = props.uiState.markerName
                pictureUri = props.uiState.mark.markerPictureUri
            }
        }

        Stack {
            sx {
                width = 100.pct
            }

            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }

                Typography {
                    variant = TypographyVariant.body1
                    + text
                }

                Typography {
                    variant = TypographyVariant.caption
                    + formattedTime
                }

            }


            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(theme.spacing(1))

                Icon {
                    color = IconColor.action
                    fontSize = IconSize.small

                    EmojiEventsIcon()
                }

                Typography {
                    variant = TypographyVariant.caption
                    + "${props.uiState.mark.courseAssignmentMark?.camMark}"
                    + "/${props.uiState.mark.courseAssignmentMark?.camMaxMark}"
                    + " ${strings[MR.strings.points]}"
                    + " "

                    if (props.uiState.camPenaltyVisible) {
                        span {
                            style = jso {
                                color = rgb(255, 0,0, 1.0)
                            }

                            + strings.format(MR.strings.late_penalty,
                                props.uiState.mark.courseAssignmentMark?.penaltyPercentage().toString() + "%"
                            )
                        }
                    }
                }
            }

            Typography {
                variant = TypographyVariant.caption
                + (props.uiState.mark.courseAssignmentMark?.camMarkerComment ?: "")
            }
        }
    }

}

val UstadCourseAssignmentMarkListItemPreview = FC<Props> {

    UstadCourseAssignmentMarkListItem {
        uiState = UstadCourseAssignmentMarkListItemUiState(
            mark = CourseAssignmentMarkAndMarkerName(
                courseAssignmentMark = CourseAssignmentMark().apply {
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camMark = 8.1f
                    camPenalty = 0.9f
                    camMaxMark = 10f
                    camLct = systemTimeInMillis()
                },
                markerFirstNames = "John",
                markerLastName = "Smith",
            )
        )
    }
}