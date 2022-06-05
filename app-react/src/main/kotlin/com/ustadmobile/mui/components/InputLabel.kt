package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.InputLabel
import mui.material.InputLabelProps
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

enum class LabelMargin {
    dense
}


fun RBuilder.umInputLabel (
    caption: String,
    htmlFor: String? = null,
    id: String? = null,
    required: Boolean? = null,
    disabled: Boolean? = null,
    error: Boolean? = null,
    focused: Boolean? = null,
    variant: FormControlVariant = FormControlVariant.standard,
    shrink: Boolean? = null,
    disableAnimation: Boolean = false,
    margin: LabelMargin? = null,
    component: String? = null,
    className: String? = null,
    handler: StyledHandler<InputLabelProps>? = null
) = createStyledComponent(InputLabel, className, handler) {
    disabled?.let { attrs.disabled = it }
    attrs.disableAnimation = disableAnimation
    htmlFor?.let { attrs.htmlFor = it }
    error?.let { attrs.error = it }
    attrs.id = id
    focused?.let { attrs.focused = it }
    margin?.let { attrs.margin = it.toString() }
    required?.let { attrs.required = it }
    shrink?.let {
        if (it) {
            attrs.shrink = it
        }
    }
    attrs.variant = variant.toString()

    childList.add(ReactNode(caption))
}