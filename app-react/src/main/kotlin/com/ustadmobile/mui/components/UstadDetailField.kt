package com.ustadmobile.mui.components

import csstype.*
import mui.icons.material.CheckBoxOutlineBlank
import mui.material.Box
import mui.material.ButtonBase
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadDetailFieldProps: Props {
    /**
     * The string for the value of the property itself
     */
    var valueText: String

    /**
     * The string for the label e.g. Phone number etc
     */
    var labelText: String

    /**
     * An icon to display for this property, if any. If null, a blank space will be inserted so the
     * text and label will be in alignment with others.
     */
    var icon: ReactNode?

    /**
     * Optional onClick handler. If an onClick handler is provided, ButtonBase will be used to
     * provide a ripple effect.
     */
    var onClick: (() -> Unit)?
}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadDetailField = FC<UstadDetailFieldProps> { props ->
    val contentNode = Stack.create {
        direction = responsive(StackDirection.row)

        Box {
            sx {
                padding = 8.px
            }

            if(props.icon != null) {
                +props.icon
            }else {
                CheckBoxOutlineBlank {
                    sx {
                        opacity = number(0.0)
                    }
                }
            }
        }


        Stack {
            direction = responsive(StackDirection.column)

            Typography {
                align = TypographyAlign.left
                variant = TypographyVariant.body1

                + props.valueText
            }

            Typography {
                align = TypographyAlign.left

                variant = TypographyVariant.caption
                + props.labelText
            }
        }
    }

    if(props.onClick == null) {
        +contentNode
    }else {
        ButtonBase {
            sx {
                justifyContent = JustifyContent.start
            }

            +contentNode
        }
    }
}