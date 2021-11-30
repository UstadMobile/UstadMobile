package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.FormControl
import mui.material.FormControlProps
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class FormControlComponent {
    div, fieldSet;

    override fun toString(): String {
        return super.toString().toHyphenCase()
    }
}

fun RBuilder.umFormControl(
    component: FormControlComponent = FormControlComponent.div,
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
        //attrs.component = component
        attrs.disabled = disabled
        attrs.error = error
        attrs.fullWidth = fullWidth
        attrs.hiddenLabel = hiddenLabel
        attrs.margin = margin.toString()
        attrs.required = required
        attrs.variant = variant.toString()
    }
}