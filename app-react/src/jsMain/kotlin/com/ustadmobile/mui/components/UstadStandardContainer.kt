package com.ustadmobile.mui.components

import csstype.PropertiesBuilder
import js.objects.jso
import mui.material.Container
import mui.material.ContainerProps
import react.FC
import react.useRequiredContext

/**
 * "Standard" Container with top padding. Top padding is not used for tabs etc that should be directly
 * under the actionbar.
 */
val UstadStandardContainer = FC<ContainerProps> { props ->
    Container {
        val theme by useRequiredContext(ThemeContext)
        + props

        (sx ?: jso<PropertiesBuilder> { }.also { sx = it }).also {
            it.paddingTop = theme.spacing(2)
        }

        if(props.maxWidth == null) {
            maxWidth = "lg"
        }

        + children
    }
}
