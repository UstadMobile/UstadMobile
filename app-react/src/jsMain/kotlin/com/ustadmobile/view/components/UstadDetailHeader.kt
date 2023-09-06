package com.ustadmobile.view.components

import mui.material.ListItem
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode

external interface UstadDetailHeaderProps: Props {
    var header: ReactNode
}

val UstadDetailHeader = FC<UstadDetailHeaderProps> {props ->
    ListItem {
        ListItemText {
            primary = props.header
        }
    }

}