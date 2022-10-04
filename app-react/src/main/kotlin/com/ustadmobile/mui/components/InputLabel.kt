package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.InputLabel
import mui.material.InputLabelMargin
import mui.material.InputLabelProps
import mui.material.InputLabelVariant
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

fun RBuilder.umInputLabel (
    caption: String,
    htmlFor: String? = null,
    id: String? = null,
    required: Boolean? = null,
    disabled: Boolean? = null,
    error: Boolean? = null,
    focused: Boolean? = null,
    variant: InputLabelVariant = InputLabelVariant.standard,
    shrink: Boolean? = null,
    disableAnimation: Boolean = false,
    margin: InputLabelMargin? = null,
    className: String? = null,
    handler: StyledHandler<InputLabelProps>? = null
) = convertFunctionalToClassElement(InputLabel, className, handler) {
    disabled?.let { attrs.disabled = it }
    attrs.disableAnimation = disableAnimation
    htmlFor?.let { attrs.htmlFor = it }
    error?.let { attrs.error = it }
    attrs.id = id
    focused?.let { attrs.focused = it }
    margin?.let { attrs.margin = it }
    required?.let { attrs.required = it }
    shrink?.let {
        if (it) {
            attrs.shrink = it
        }
    }
    attrs.variant = variant

    childList.add(ReactNode(caption))
}