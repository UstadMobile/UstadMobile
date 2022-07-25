package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import com.ustadmobile.util.StyleManager
import kotlinx.css.hyphenize
import mui.material.FormControl
import mui.material.FormControlMargin
import mui.material.FormControlProps
import mui.material.FormControlVariant
import react.RBuilder
import styled.StyledHandler
import styled.css


fun RBuilder.umFormControl(
    disabled: Boolean = false,
    error: Boolean = false,
    fullWidth: Boolean = false,
    margin: FormControlMargin = FormControlMargin.none,
    required: Boolean = false,
    variant: FormControlVariant = FormControlVariant.standard,
    hiddenLabel: Boolean = false,
    className: String? = null,
    handler: StyledHandler<FormControlProps>? = null
) {
    createReUsableComponent(FormControl, className, handler) {
        attrs.disabled = disabled
        attrs.error = error
        attrs.fullWidth = fullWidth
        attrs.hiddenLabel = hiddenLabel
        attrs.margin = margin
        attrs.required = required
        attrs.variant = variant
        css(StyleManager.defaultFullWidth)
    }
}