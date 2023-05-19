package com.ustadmobile.view.components

import com.ustadmobile.core.impl.appstate.FabUiState
import csstype.Position
import csstype.px
import mui.icons.material.Add
import mui.icons.material.Edit
import mui.material.Fab
import mui.material.FabColor
import mui.material.FabVariant
import mui.system.sx
import react.FC
import react.Props
import react.create

private val ICON_MAP = mapOf<FabUiState.FabIcon, FC<*>>(
    FabUiState.FabIcon.ADD to Add,
    FabUiState.FabIcon.EDIT to Edit,
)

external interface UstadFabProps: Props {
    var fabState: FabUiState
}

val UstadFab = FC <UstadFabProps> { props ->
    if(props.fabState.visible) {
        Fab {
            sx {
                position = Position.fixed
                right = 20.px
                bottom = 20.px
            }

            color = FabColor.secondary
            variant = FabVariant.extended
            onClick = {
                props.fabState.onClick()
            }

            + ICON_MAP[props.fabState.icon]?.create()

            + (props.fabState.text ?: "")
        }
    }
}

