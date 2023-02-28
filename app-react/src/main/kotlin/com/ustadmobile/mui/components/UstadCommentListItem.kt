package com.ustadmobile.mui.components

import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Person
import mui.icons.material.AccountCircle
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create


external interface UstadCommentListItemProps : Props {

    var commentwithperson: CommentsWithPerson

    var onClickComment: (CommentsWithPerson) -> Unit

}

val UstadCommentListItem = FC<UstadCommentListItemProps> { props ->

    val formattedTime = useFormattedTime(props.commentwithperson.commentsDateTimeAdded.toInt())

    ListItem {
        ListItemButton {
            onClick = { props.onClickComment(props.commentwithperson) }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(props.commentwithperson.commentsPerson?.fullName() ?: "")
                secondary = ReactNode(props.commentwithperson.commentsText ?: "")
            }
        }

        secondaryAction = Typography.create {
            + formattedTime
        }
    }

}

val UstadCommentListItemPreview = FC<Props> {

    UstadCommentListItem {
        commentwithperson = CommentsWithPerson().apply {
            commentsUid = 1
            commentsPerson = Person().apply {
                firstNames = "Bob"
                lastName = "Dylan"
            }
            commentsText = "I like this activity. Shall we discuss this in our next meeting?"
        }
    }
}