package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzassignment.isFromSubmitterGroup
import com.ustadmobile.lib.db.composites.CommentsAndName
import kotlinx.datetime.TimeZone
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.mui.components.UstadLinkify
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.core.MR
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.wrappers.intl.Intl
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

external interface UstadCommentListItemProps : Props {

    var commentsAndName: CommentsAndName?

    var dateTimeNow: LocalDateTime

    var timeFormatter: Intl.Companion.DateTimeFormat

    var dateFormatter: Intl.Companion.DateTimeFormat

    var dayOfWeekMap: Map<DayOfWeek, String>

}

val UstadCommentListItem = FC<UstadCommentListItemProps> { props ->

    val strings = useStringProvider()

    val formattedTime = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.dateTimeNow,
        timestamp = props.commentsAndName?.comment?.commentsDateTimeAdded ?: 0L,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dateFormatter = props.dateFormatter,
        dayOfWeekStringMap = props.dayOfWeekMap
    )

    val groupNumSuffix = props.commentsAndName?.comment
        ?.takeIf { it.isFromSubmitterGroup }
        ?.let { "(${strings[MR.strings.group]} ${it.commentsFromSubmitterUid})"}
        ?: ""


    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                personName = "${props.commentsAndName?.firstNames ?: ""} ${props.commentsAndName?.lastName ?: ""}"
                pictureUri = props.commentsAndName?.pictureUri
            }
        }

        ListItemText {
            primary = ReactNode(
        "${props.commentsAndName?.firstNames} ${props.commentsAndName?.lastName} $groupNumSuffix"
            )
            secondary = UstadLinkify.create {
                + (props.commentsAndName?.comment?.commentsText ?: "")
            }
        }

        secondaryAction = ReactNode(formattedTime)
    }

}

val UstadCommentListItemPreview = FC<Props> {

    UstadCommentListItem {
        commentsAndName = CommentsAndName().apply {
            comment = Comments().apply {
                commentsUid = 1
                commentsText = "I like this activity. Shall we discuss this in our next meeting?"
            }

            firstNames = "Bob"
            lastName = "Dylan"
        }
    }
}
