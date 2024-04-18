package com.ustadmobile.view.clazz.list

import com.ustadmobile.core.hooks.useStringProvider
import mui.material.ListItem
import mui.material.ListItemText
import mui.material.Tooltip
import react.FC
import react.Props
import react.ReactNode
import mui.icons.material.Close as CloseIcon
import com.ustadmobile.core.MR
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndCoursePic
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.clazz.uriOrDefaultBanner
import com.ustadmobile.wrappers.intl.Intl
import js.objects.jso
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import mui.material.Avatar
import mui.material.IconButton
import mui.material.ListItemIcon
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.create
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.useRequiredContext
import mui.icons.material.Schedule as ScheduleIcon

external interface PendingEnrolmentListItemProps: Props {
    var request: EnrolmentRequestAndCoursePic

    var onClickCancel: (EnrolmentRequest) -> Unit

    var timeNow: LocalDateTime

    var timeFormatter: Intl.Companion.DateTimeFormat

    var dateFormatter: Intl.Companion.DateTimeFormat

    var dayOfWeekStrings: Map<DayOfWeek, String>


}

val PendingEnrolmentListItem = FC<PendingEnrolmentListItemProps> { props ->
    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    val request = props.request.enrolmentRequest

    val requestTimeStr = useDayOrDate(
        enabled = true,
        localDateTimeNow  = props.timeNow,
        timestamp = request?.erRequestTime ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        dateFormatter = props.dateFormatter,
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dayOfWeekStringMap = props.dayOfWeekStrings,
    )

    ListItem {
        ListItemIcon {
            Avatar {
                src = props.request.coursePicture.uriOrDefaultBanner(
                    props.request.enrolmentRequest?.erClazzName ?: ""
                )
            }
        }

        ListItemText {
            primary = ReactNode(props.request.enrolmentRequest?.erClazzName ?: "")
            secondary = Stack.create {
                direction = responsive(StackDirection.row)

                ScheduleIcon {
                    sx {
                        marginRight = theme.spacing(1)
                    }

                    fontSize = SvgIconSize.small
                }

                + requestTimeStr

            }
            secondaryTypographyProps = jso {
                component = div
            }
        }

        secondaryAction = Tooltip.create {
            title = ReactNode(strings[MR.strings.cancel])
            IconButton {
                onClick = {  props.request.enrolmentRequest?.also(props.onClickCancel)  }
                ariaLabel = strings[MR.strings.cancel]
                CloseIcon()
            }
        }


    }
}