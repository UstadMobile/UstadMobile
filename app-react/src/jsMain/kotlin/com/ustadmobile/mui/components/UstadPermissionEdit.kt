package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.view.components.UstadSwitchField
import dev.icerock.moko.resources.StringResource
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import mui.system.sx
import react.useRequiredContext

external interface UstadPermissionEditProps: Props {
    var value: Long

    var permissionLabels: List<Pair<StringResource, Long>>

    var onToggle: (Long) -> Unit

    var enabled: Boolean

}

val UstadPermissionEditComponent = FC<UstadPermissionEditProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)
    val spacing = theme.spacing(1)

    Stack {
        direction = responsive(StackDirection.column)

        props.permissionLabels.forEach { permissionLabel ->
            UstadSwitchField {
                sx {
                    paddingTop = spacing
                    paddingBottom = spacing
                }

                label = strings[permissionLabel.first]
                checked =  props.value.hasFlag(permissionLabel.second)
                enabled = props.enabled
                onChanged = {
                    props.onToggle(permissionLabel.second)
                }
            }
        }
    }
}

