package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.util.ext.assignPropsTo
import js.objects.jso
import mui.icons.material.Send as SendIcon
import mui.material.IconButton
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.InputBaseProps
import mui.material.TextFieldProps
import react.FC
import react.create
import react.dom.aria.ariaLabel
import react.useMemo

external interface UstadSendTextFieldProps: TextFieldProps {

    var onClickSend: () -> Unit

}

/**
 * Simple TextField that will show a send button as an Adornment when the text in the field is not
 * blank.
 */
val UstadSendTextField = FC<UstadSendTextFieldProps> { props ->
    val strings = useStringProvider()

    println("UstadSendTextField: value = ${props.value} / ${props.value as? String}")
    val isBlank = useMemo(props.value) {
        (props.value as? String).isNullOrBlank()
    }

    UstadTextField {
        props.assignPropsTo(
            receiver = this,
            filter = { it != "InputProps" }
        )

        onKeyUp = { evt ->
            if(evt.key == "Enter" && !isBlank && !evt.ctrlKey && !evt.shiftKey)
                props.onClickSend()
        }

        if(!isBlank) {
            asDynamic().InputProps = jso<InputBaseProps> {
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    IconButton {
                        onClick = {
                            props.onClickSend()
                        }
                        props.id?.also {
                            id = "${it}_send_button"
                        }
                        ariaLabel = strings[MR.strings.send]
                        SendIcon()
                    }
                }
            }
        }
    }
}