package com.ustadmobile.mui.components

import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface DetailRowProps: Props {
    var valueText: String

    var labelText: String

    var icon: ReactNode
}

val DetailRow = FC<DetailRowProps> { props ->
    Stack {
        direction = responsive(StackDirection.row)

        +props.icon

        Stack {
            direction = responsive(StackDirection.column)
            Typography {
                variant = TypographyVariant.body1

                + props.valueText
            }

            Typography {
                variant = TypographyVariant.caption
                + props.labelText
            }
        }

    }
}