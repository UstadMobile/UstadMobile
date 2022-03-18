package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Snackbar
import mui.material.SnackbarProps
import react.RBuilder
import react.ReactElement
import styled.StyledHandler
import kotlin.js.json

@Suppress("EnumEntryName")
enum class SnackbarHorizAnchor {
    left, center, right
}


fun RBuilder.umSnackbar(
    message: ReactElement?,
    open: Boolean? = null,
    onClose: (() -> Unit)? = null,
    horizAnchor: SnackbarHorizAnchor = SnackbarHorizAnchor.center,
    key: String? = null,
    autoHideDuration: Int? = null,
    resumeHideDuration: Int? = null,
    className: String? = null,
    handler: StyledHandler<SnackbarProps>? = null
) = createStyledComponent(Snackbar, className, handler) {
    autoHideDuration?.let { attrs.autoHideDuration = it }
    attrs.asDynamic().anchorOrigin = json("vertical" to "bottom",
        "horizontal" to horizAnchor.toString())
    attrs.key = "horizontal+${horizAnchor}"
    key?.let { attrs.key = it }
    message?.let { attrs.message = message}
    attrs.onClose = { event, reason ->
        onClose?.invoke()
    }
    open?.let { attrs.open = it }
    resumeHideDuration?.let {
        attrs.resumeHideDuration = it
    }
}

/**
 * Builder for Snackbar with a message of type string.
 */
fun RBuilder.umSnackbar(
    message: String,
    open: Boolean? = null,
    onClose: (() -> Unit)? = null,
    horizAnchor: SnackbarHorizAnchor = SnackbarHorizAnchor.center,
    key: String? = null,
    autoHideDuration: Int? = null,
    resumeHideDuration: Int? = null,
    className: String? = null,
    handler: StyledHandler<SnackbarProps>? = null
) {
    @Suppress("UnsafeCastFromDynamic")
    val dynamicElement: ReactElement = message.asDynamic()
    umSnackbar(dynamicElement, open, onClose, horizAnchor, key, autoHideDuration, resumeHideDuration, className, handler)
}