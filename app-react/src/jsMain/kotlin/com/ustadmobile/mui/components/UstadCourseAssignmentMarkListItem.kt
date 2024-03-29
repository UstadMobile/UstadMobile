package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.penaltyPercentage
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.wrappers.intl.Intl
import web.cssom.rgb
import js.objects.jso
import kotlinx.datetime.TimeZone
import mui.icons.material.EmojiEvents as EmojiEventsIcon
import mui.material.*
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.span

external interface UstadCourseAssignmentMarkListItemProps : Props {

    var uiState: UstadCourseAssignmentMarkListItemUiState

    var timeFormatter: Intl.Companion.DateTimeFormat

    var dateFormatter: Intl.Companion.DateTimeFormat

}

val UstadCourseAssignmentMarkListItem = FC<UstadCourseAssignmentMarkListItemProps> { props ->

    val strings = useStringProvider()

    var text = props.uiState.markerName

    if (props.uiState.markerGroupNameVisible){
        text += " (${strings.format(MR.strings.group_number, props.uiState.peerGroupNumber.toString())})"
    }

    val dayOrDateStr = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.uiState.localDateTimeNow,
        timestamp = props.uiState.mark.courseAssignmentMark?.camLct ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dateFormatter = props.dateFormatter,
        dayOfWeekStringMap = props.uiState.dayOfWeekStrings,
    )

    ListItem{

        secondaryAction = Typography.create {
            variant = TypographyVariant.caption
            + dayOrDateStr
        }

        ListItemIcon {
            UstadPersonAvatar {
                personName = props.uiState.markerName
                pictureUri = props.uiState.mark.markerPictureUri
            }
        }

        ListItemText {
            primary = ReactNode(text)
            secondary = span.create {
                EmojiEventsIcon {
                    color = SvgIconColor.action
                    fontSize = SvgIconSize.small
                }

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

                br()

                + (props.uiState.mark.courseAssignmentMark?.camMarkerComment ?: "")
            }

            secondaryTypographyProps = jso {
                component = ReactHTML.div
            }
        }
    }


}
