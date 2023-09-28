package com.ustadmobile.mui.components

import web.cssom.AlignContent
import web.cssom.Display
import web.cssom.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.dom.events.MouseEventHandler

external interface UstadQuickActionButtonProps : Props {
    var onClick: MouseEventHandler<*>

    var text: String

    var icon: react.ReactNode

    var id: String?

}

val UstadQuickActionButton = FC<UstadQuickActionButtonProps> { props ->
    Button {
        variant = ButtonVariant.text
        onClick = props.onClick
        id = props.id

        Stack {
            direction = responsive(StackDirection.column)

            Box {
                sx {
                    width = 80.px
                    alignContent = AlignContent.center
                    display = Display.block
                }
                +props.icon
            }

            Typography {
                align = TypographyAlign.center
                +props.text
            }
        }
    }
}