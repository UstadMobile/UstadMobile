package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager
import kotlinx.css.hyphenize
import mui.material.FormControl
import mui.material.FormControlProps
import react.RBuilder
import styled.StyledHandler
import styled.css

@Suppress("EnumEntryName")
enum class FormControlComponent {
    div, fieldSet;

    override fun toString(): String {
        return super.toString().hyphenize()
    }
}

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
    createStyledComponent(FormControl, className, handler) {
        attrs.disabled = disabled
        attrs.error = error
        attrs.fullWidth = fullWidth
        attrs.hiddenLabel = hiddenLabel
        attrs.margin = margin.toString()
        attrs.required = required
        attrs.variant = variant.toString()
        css(StyleManager.defaultFullWidth)
    }
}