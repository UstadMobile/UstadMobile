package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Snackbar
import mui.material.SnackbarProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class SnackbarHorizAnchor {
    left, center, right
}

@Suppress("EnumEntryName")
enum class SnackbarVertAnchor {
    top, bottom
}


fun RBuilder.umSnackbar(
    message: ReactElement?,
    open: Boolean? = null,
    onClose: ((Event, Any) -> Unit)? = null,
    horizAnchor: SnackbarHorizAnchor = SnackbarHorizAnchor.center,
    vertAnchor: SnackbarVertAnchor = SnackbarVertAnchor.bottom,
    key: String? = null,
    autoHideDuration: Int? = null,
    resumeHideDuration: Int? = null,
    className: String? = null,
    handler: StyledHandler<SnackbarProps>? = null
) = createStyledComponent(Snackbar, className, handler) {
    attrs.asDynamic().anchorOriginHorizontal = horizAnchor
    attrs.asDynamic().anchorOriginVertical = vertAnchor
    autoHideDuration?.let { attrs.autoHideDuration = it }
    key?.let { attrs.key = it }
    message?.let { attrs.message = message}
    attrs.onClose = { event, reason ->
        onClose?.invoke(event.nativeEvent, reason)
    }
    open?.let { attrs.open = it }
    resumeHideDuration?.let { attrs.resumeHideDuration = it }
}

/**
 * Builder for Snackbar with a message of type string.
 */
fun RBuilder.umSnackbar(
    message: String,
    open: Boolean? = null,
    onClose: ((Event, Any) -> Unit)? = null,
    horizAnchor: SnackbarHorizAnchor = SnackbarHorizAnchor.center,
    vertAnchor: SnackbarVertAnchor = SnackbarVertAnchor.bottom,
    key: String? = null,
    autoHideDuration: Int? = null,
    resumeHideDuration: Int? = null,

    className: String? = null,
    handler: StyledHandler<SnackbarProps>? = null
) {
    @Suppress("UnsafeCastFromDynamic")
    val dynamicElement: ReactElement = message.asDynamic()
    umSnackbar(dynamicElement, open, onClose, horizAnchor, vertAnchor, key, autoHideDuration, resumeHideDuration, className, handler)
}