package com.ustadmobile.mui.components

import mui.icons.material.AccountCircle
import mui.icons.material.Add
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadAddCommentListItemProps: Props {

    var text: String

    var enabled: Boolean?

    var personUid: Long?

    var onClickAddComment: () -> Unit

}

val UstadAddCommentListItem = FC<UstadAddCommentListItemProps> { props ->

    ListItem {

        ListItemIcon {
            + AccountCircle.create()
        }

        ListItemButton{

            disabled = !(props.enabled ?: true)

            ListItemText{
                primary = ReactNode(props.text)
            }

            onClick = {
                props.onClickAddComment()
            }
        }
    }
}

val UstadAddCommentListItemPreview = FC<Props> {

    UstadAddCommentListItem {
        text = "Add"
        enabled = true
        personUid = 0
        onClickAddComment = {}
    }
}