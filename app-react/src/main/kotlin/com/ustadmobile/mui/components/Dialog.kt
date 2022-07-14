package com.ustadmobile.mui.components

import Breakpoint
import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.*
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umDialog(
    open: Boolean,
    onClose: () -> Unit,
    fullWidth: Boolean = true,
    maxWidth: Breakpoint = Breakpoint.sm,
    className: String? = null,
    handler: StyledHandler<DialogProps>? = null
) = createStyledComponent(Dialog, className, handler){
    attrs.open = open
    attrs.fullWidth = fullWidth
    attrs.maxWidth = maxWidth
    attrs.onClose = { _, _ ->
        onClose.invoke()
    }
}


fun RBuilder.umDialogTitle(
    title: String,
    className: String? = null,
    handler: StyledHandler<DialogTitleProps>? = null
) = createStyledComponent(DialogTitle, className, handler){

    attrs.title = title
}


fun RBuilder.umDialogActions(
    className: String? = null,
    handler: StyledHandler<DialogActionsProps>? = null
) = createStyledComponent(DialogActions, className, handler)


fun RBuilder.umDialogContent(
    className: String? = null,
    handler: StyledHandler<DialogContentProps>? = null
) = createStyledComponent(DialogContent, className, handler)