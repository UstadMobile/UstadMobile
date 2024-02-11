package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.appstate.AppActionButton
import com.ustadmobile.mui.ext.iconComponent
import mui.material.IconButton
import mui.material.Tooltip
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import web.cssom.ColorProperty

external interface UstadActionButtonIconProps: Props {
    var actionButton: AppActionButton

    var color: ColorProperty?

}

val UstadActionButtonIcon = FC<UstadActionButtonIconProps> { props ->
    Tooltip {
        title = ReactNode(props.actionButton.contentDescription)

        IconButton {
            ariaLabel = props.actionButton.contentDescription
            id = props.actionButton.id

            onClick = {
                props.actionButton.onClick()
            }

            + props.actionButton.iconComponent.create {
                props.color?.also {
                    sx {
                        color = it
                    }
                }
            }
        }
    }

}
