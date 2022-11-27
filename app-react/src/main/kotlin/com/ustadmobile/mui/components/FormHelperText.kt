package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.FormHelperText
import mui.material.FormHelperTextMargin
import mui.material.FormHelperTextProps
import mui.material.FormHelperTextVariant
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

fun RBuilder.umFormHelperText (
    caption: String,
    disabled: Boolean = false,
    error: Boolean = false,
    filled: Boolean = false,
    focused: Boolean = false,
    required: Boolean = false,
    variant: FormHelperTextVariant = FormHelperTextVariant.standard,
    margin: FormHelperTextMargin? = null,
    component: String? = null,
    className: String? = null,
    handler: StyledHandler<FormHelperTextProps>? = null
) {
    convertFunctionalToClassElement(FormHelperText, className, handler) {
        component?.let { attrs.asDynamic().component = it }
        attrs.disabled = disabled
        attrs.error = error
        attrs.filled = filled
        attrs.focused = focused
        margin?.let {
            attrs.margin = it
        }
        attrs.required = required
        attrs.variant = variant
        childList.add(ReactNode(caption))
    }
}