package com.ustadmobile.mui.components

import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.WhiteSpace
import web.cssom.pct
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.PropsWithSx
import mui.system.sx
import react.FC
import react.PropsWithChildren

external interface SingleLineTypographyProps: PropsWithChildren, PropsWithSx {
    var variant: TypographyVariant
    var paragraph: Boolean?
}

val SingleLineTypography = FC<SingleLineTypographyProps> { props ->
    Typography {
        variant = props.variant
        sx {
            whiteSpace = WhiteSpace.nowrap
            overflow = Overflow.hidden
            textOverflow = TextOverflow.ellipsis
            maxWidth = 100.pct
        }

        + props.children
    }
}