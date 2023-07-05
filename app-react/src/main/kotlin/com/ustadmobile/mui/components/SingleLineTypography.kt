package com.ustadmobile.mui.components

import csstype.Overflow
import csstype.TextOverflow
import csstype.WhiteSpace
import csstype.pct
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