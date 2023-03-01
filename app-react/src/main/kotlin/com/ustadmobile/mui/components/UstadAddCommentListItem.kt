package com.ustadmobile.mui.components

import csstype.*
import mui.icons.material.AccountCircle
import mui.icons.material.Add
import mui.material.*
import mui.system.responsive
import mui.system.sx
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
            sx {
                backgroundColor = rgba(0, 0,0, 0.11)
                borderRadius = 8.px
            }
            ListItemText{
                primary = ReactNode(props.text)
                sx {
                    color = rgba(0, 0,0, 0.57)
                }
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