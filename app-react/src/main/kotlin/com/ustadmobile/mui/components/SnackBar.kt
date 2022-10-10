package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.*
import react.RBuilder
import react.ReactNode
import styled.StyledHandler


fun RBuilder.umSnackbar(
    message: String,
    open: Boolean? = null,
    onClose: (() -> Unit)? = null,
    horizontalAnchor: SnackbarOriginHorizontal = SnackbarOriginHorizontal.center,
    verticalAnchor: SnackbarOriginVertical = SnackbarOriginVertical.top,
    key: String? = null,
    autoHideDuration: Int? = null,
    resumeHideDuration: Int? = null,
    className: String? = null,
    handler: StyledHandler<SnackbarProps>? = null
) = convertFunctionalToClassElement(Snackbar, className, handler) {
    autoHideDuration?.let { attrs.autoHideDuration = it }
    attrs.key = "horizontal+${horizontalAnchor.name}"
    attrs.anchorOrigin?.horizontal = horizontalAnchor
    attrs.anchorOrigin?.vertical = verticalAnchor
    key?.let { attrs.key = it }
    attrs.message = ReactNode(message)
    attrs.onClose = { _, _ -> onClose?.invoke() }
    open?.let { attrs.open = it }
    resumeHideDuration?.let {
        attrs.resumeHideDuration = it
    }
}