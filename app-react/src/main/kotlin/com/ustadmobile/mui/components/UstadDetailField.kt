package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import csstype.*
import mui.icons.material.AccountCircle
import mui.icons.material.CheckBoxOutlineBlank
import mui.icons.material.Visibility
import mui.material.*
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

    /**
     * Optional secondary space (will be end-aligned)
     */
    var secondaryActionContent: ReactNode?
}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadDetailField = FC<UstadDetailFieldProps> { props ->
    val contentNode = Grid.create {
        //direction = responsive(mui.material.StackDirection.row)
        container = true

        Grid {
            item = true
            xs = 2
            md = 1

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
        }

        Grid {
            item = true
            xs = 8
            md = 10

            Stack {
                direction = responsive(StackDirection.column)

                Typography {
                    sx {

                    }
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

        if(props.secondaryActionContent != null) {
            Grid {
                sx {
                    justifyContent = JustifyContent.end
                }

                xs = 2
                md = 1

                +props.secondaryActionContent
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

val UstadDetailFieldPreview = FC<Props> {
    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                valueText = "Demo value"
                labelText = "With icon and secondary action"
                icon = AccountCircle.create()
                secondaryActionContent = IconButton.create {
                    onClick = { }
                    Visibility {

                    }
                }
            }

            UstadDetailField {
                valueText = "Demo value"
                labelText = "With icon and secondary action"
                icon = AccountCircle.create()
            }

            UstadDetailField {
                valueText = "Demo value"
                labelText = "With no icon or secondary action"
            }
        }
    }
}

