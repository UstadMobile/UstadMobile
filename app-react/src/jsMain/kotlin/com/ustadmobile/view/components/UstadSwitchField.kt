package com.ustadmobile.view.components

import web.cssom.*
import js.objects.jso
import mui.material.*
import mui.system.PropsWithSx
import mui.system.sx
import react.FC
import react.ReactNode
import react.create
import emotion.css.ClassName as EmotionCssClassName

private val labelClassName : ClassName by lazy {
    EmotionCssClassName {
        width = 100.pct
    }
}


external interface UstadSwitchFieldProps: PropsWithSx {

    /**
     * True if the switch is on, false otherwise
     */
    var checked: Boolean

    /**
     * Event handler
     */
    var onChanged: (Boolean) -> Unit

    /**
     * Label for switch
     */
    var label: String

    /**
     * Enabled - effectively true by default
     */
    var enabled: Boolean?

    /**
     * Error string to show (if any)
     */
    var error: String?

    /**
     * DOM id
     */
    var id: String?

}

/**
 * Standard switch implementation. This will show the label at the start (e.g. left by default),
 * and the switch itself to the end (e.g. right by default) as per normal material design.
 */
val UstadSwitchField = FC<UstadSwitchFieldProps> { props ->
    FormControl {
        error = props.error != null
        sx {
            + props.sx
        }

        FormControlLabel {
            classes = jso {
                label = labelClassName
            }

            disabled = (props.enabled == false)
            label = ReactNode(props.label)
            labelPlacement = LabelPlacement.start
            control = Switch.create {
                checked = props.checked
                id = props.id
                onChange = { _, value ->
                    props.onChanged(value)
                }
            }
        }

        val helperText = props.error
        if(helperText != null) {
            FormHelperText {
                +helperText
            }
        }
    }

}


