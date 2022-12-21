package com.ustadmobile.mui.components

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useFormattedDate
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.*
import mui.icons.material.AccountCircle
import mui.icons.material.CalendarToday
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

external interface UstadMessageFieldProps: Props {
    /**
     * The string for the value of the property itself
     */
    var secondText: String

    /**
     * The string for the label e.g. Phone number etc
     */
    var firstText: String

    var thirdText: String
    var fourthText: String
    var fifthText: String

    /**
     * An icon to display for this property, if any. If null, a blank space will be inserted so the
     * text and label will be in alignment with others.
     */
    var icon: ReactNode?

    var thirdTextIcon: ReactNode?

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
 * Base component for showing message fields .
 */
val UstadMessageField = FC<UstadMessageFieldProps> { props ->
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
                    textAlign = TextAlign.center
                }

                if(props.icon != null) {
                    +props.icon
                }else {
                    UstadBlankIcon { }
                }
            }
        }

        Grid {
            item = true
            xs = 8
            md = 10

            Stack {
                direction = responsive(StackDirection.column)






                Grid {
                    item = true
                    sx {
                        justifyContent = JustifyContent.end
                    }

                    xs = 5
                    md = 10


                    Typography {
                        sx {

                        }
                        align = TypographyAlign.left
                        variant = TypographyVariant.body1

                        + props.firstText
                    }

                    Typography {
                        sx {

                        }
                        align = TypographyAlign.right
                        variant = TypographyVariant.subtitle1

                        + props.fourthText
                    }
                }






                Grid {
                    item = true
                    sx {
                        justifyContent = JustifyContent.end
                    }

                    xs = 5
                    md = 10


                    Typography {
                        align = TypographyAlign.left

                        variant = TypographyVariant.body1
                        + props.secondText
                    }

                    Typography {
                        sx {

                        }
                        align = TypographyAlign.right
                        variant = TypographyVariant.subtitle1

                        + props.fifthText
                    }
                }

                Grid {
                    item = true
                    sx {
                        justifyContent = JustifyContent.end
                    }

                    xs = 2
                    md = 10


                    +props.thirdTextIcon

                    Typography {
                        align = TypographyAlign.left

                        variant = TypographyVariant.caption
                        + props.thirdText
                    }
                }




            }

        }

        if(props.secondaryActionContent != null) {
            Grid {
                item = true
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
                padding = 0.px
                margin = 0.px
                border = 0.px
            }

            +contentNode
        }
    }
}

val UstadMessageFieldPreview = FC<Props> {
    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadMessageField {
                secondText = "Demo value"
                firstText = "With icon and secondary action"
                icon = AccountCircle.create()
                secondaryActionContent = IconButton.create {
                    onClick = { }
                    Visibility {

                    }
                }
            }

            UstadMessageField {
                secondText = "Demo value"
                firstText = "With icon and secondary action"
                icon = AccountCircle.create()
            }

            UstadMessageField {
                secondText = "Demo value"
                firstText = "With no icon or secondary action"
            }


            UstadMessageField {
                secondText = "Demo value"
                firstText = "Clickable with icon and secondary action"
                icon = AccountCircle.create()
                onClick = {}
            }

            UstadMessageField {
                secondText = useFormattedDate(systemTimeInMillis(), "Asia/Dubai")
                firstText = "Date"
                icon = CalendarToday.create()
            }
        }
    }
}

