package com.ustadmobile.mui.components

import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Person
import csstype.px
import mui.icons.material.Person as PersonIcon
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create


external interface UstadCommentListItemProps : Props {

    var commentWithPerson: CommentsWithPerson

    var onClickComment: (CommentsWithPerson) -> Unit

}

val UstadCommentListItem = FC<UstadCommentListItemProps> { props ->

    val formattedTime = useFormattedTime(props.commentWithPerson.commentsDateTimeAdded.toInt())

    ListItem {
        ListItemButton {
            onClick = { props.onClickComment(props.commentWithPerson) }

            ListItemIcon {
                + PersonIcon.create {
                    sx {
                        width = 40.px
                        height = 40.px
                    }
                }
            }

            ListItemText {
                primary = ReactNode(props.commentWithPerson.commentsPerson?.fullName() ?: "")
                secondary = ReactNode(props.commentWithPerson.commentsText ?: "")
            }
        }

        secondaryAction = Typography.create {
            + formattedTime
        }
    }

}

val UstadCommentListItemPreview = FC<Props> {

    UstadCommentListItem {
        commentWithPerson = CommentsWithPerson().apply {
            commentsUid = 1
            commentsPerson = Person().apply {
                firstNames = "Bob"
                lastName = "Dylan"
            }
            commentsText = "I like this activity. Shall we discuss this in our next meeting?"
        }
    }
}