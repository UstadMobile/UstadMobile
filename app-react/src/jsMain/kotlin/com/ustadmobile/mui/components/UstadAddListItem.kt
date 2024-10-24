package com.ustadmobile.mui.components

import mui.icons.material.Add as AddIcon
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadAddListItemProps: Props {

    var text: String

    var enabled: Boolean?

    var icon: ReactNode?

    var onClickAdd: () -> Unit

    var id: String?

    var disableGutters: Boolean?

}

val UstadAddListItem = FC<UstadAddListItemProps> { props ->

    ListItem {
        ListItemButton{
            props.id?.also {
                id = it
            }

            props.disableGutters?.also {
                disableGutters = it
            }

            disabled = !(props.enabled ?: true)

            ListItemIcon {
                + (props.icon ?: AddIcon.create())
            }

            ListItemText{
                primary = ReactNode(props.text)
            }

            onClick = {
                props.onClickAdd()
            }
        }
    }
}

val UstadAddListItemPreview = FC<Props> {

    UstadAddListItem {
        text = "Add"
        enabled = true
        icon = AddIcon.create()
        onClickAdd = {}
    }
}