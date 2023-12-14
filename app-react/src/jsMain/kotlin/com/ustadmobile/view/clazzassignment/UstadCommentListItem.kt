package com.ustadmobile.view.clazzassignment

import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.CommentsAndName
import web.cssom.px
import kotlinx.datetime.TimeZone
import mui.icons.material.Person as PersonIcon
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import com.ustadmobile.lib.db.entities.Comments


external interface UstadCommentListItemProps : Props {

    var commentsAndName: CommentsAndName?

}

val UstadCommentListItem = FC<UstadCommentListItemProps> { props ->

    val formattedTime = useFormattedDateAndTime(
        timeInMillis = props.commentsAndName?.comment?.commentsDateTimeAdded ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    ListItem {

        ListItemIcon {
            + PersonIcon.create {
                sx {
                    width = 40.px
                    height = 40.px
                }
            }
        }

        ListItemText {
            primary = ReactNode("${props.commentsAndName?.firstNames} ${props.commentsAndName?.lastName}")
            secondary = ReactNode(props.commentsAndName?.comment?.commentsText ?: "")
        }

        secondaryAction = Typography.create {
            + formattedTime
        }
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
