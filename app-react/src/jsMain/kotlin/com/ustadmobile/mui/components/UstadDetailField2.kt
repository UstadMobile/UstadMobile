package com.ustadmobile.mui.components

import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode

external interface UstadDetailField2Props: Props {

    var valueContent: ReactNode

    var labelContent: ReactNode

    var leadingContent: ReactNode?

    var onClick: (() -> Unit)?

}

val UstadDetailField2 = FC<UstadDetailField2Props> { props ->
    ListItem {
        val onClickVal = props.onClick
        if(onClickVal != null) {
            ListItemButton {
                disableGutters = true
                onClick = { onClickVal() }
                UstadDetailField2Content {
                    + props
                }
            }
        }else {
            UstadDetailField2Content {
                + props
            }
        }
    }
}

private val UstadDetailField2Content = FC<UstadDetailField2Props> { props ->
    props.leadingContent?.also { leadingContent ->
        ListItemIcon {
            + leadingContent
        }
    }

    ListItemText {
        primary = props.valueContent
        secondary = props.labelContent
    }
}
