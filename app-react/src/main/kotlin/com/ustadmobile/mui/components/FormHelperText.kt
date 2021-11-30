package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.FormHelperText
import mui.material.FormHelperTextProps
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
    variant: FormControlVariant = FormControlVariant.standard,
    margin: LabelMargin? = null,
    component: String? = null,
    className: String? = null,
    handler: StyledHandler<FormHelperTextProps>? = null
) {
    createStyledComponent(FormHelperText, className, handler) {
        component?.let { attrs.asDynamic().component = it }
        attrs.disabled = disabled
        attrs.error = error
        attrs.filled = filled
        attrs.focused = focused
        margin?.let {
            attrs.margin = it.toString()
        }
        attrs.required = required
        attrs.variant = variant.toString()
        childList.add(ReactNode(caption))
    }
}