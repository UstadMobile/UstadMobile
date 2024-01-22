package com.ustadmobile.view.clazzassignment

import com.ustadmobile.hooks.useFormattedDateAndTime
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
            UstadPersonAvatar {
                personName = "${props.commentsAndName?.firstNames ?: ""} ${props.commentsAndName?.lastName ?: ""}"
                pictureUri = props.commentsAndName?.pictureUri
            }
        }

        ListItemText {
            primary = ReactNode("${props.commentsAndName?.firstNames} ${props.commentsAndName?.lastName}")
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
