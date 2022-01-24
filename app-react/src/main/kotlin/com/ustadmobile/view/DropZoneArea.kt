package com.ustadmobile.view

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.UmState
import react.ComponentType
import react.RBuilder
import react.RComponent
import react.ReactNode
import styled.StyledHandler
import styled.StyledProps

@JsModule("material-ui-dropzone")
@JsNonModule
private external val dropZoneModule: dynamic


@JsModule("@material-ui/core/Icon")
@JsNonModule
private external val iconModule: dynamic
private val iconComponentType: RComponent<IconV4, UmState> = iconModule.default

external interface IconV4 : StyledProps{}

fun RBuilder.iconV4(
    iconName: String,
    className: String? = null,
    handler: StyledHandler<IconV4>? = null
) = createStyledComponent(dropZoneComponent.unsafeCast<ComponentType<IconV4>>(), className, handler) {
    childList.add(ReactNode(iconName))
}



@Suppress("UnsafeCastFromDynamic")
private val dropZoneComponent: RComponent<GoogleChartsProps, UmState> = dropZoneModule.DropzoneAreaBase

external interface DropZoneProps: StyledProps {
    var clearOnUnmount: Boolean
    var showPreviews: Boolean
    var showFileNames: Boolean
    var showAlerts: Boolean
    var initialFiles: Array<dynamic>
    var acceptedFiles: Array<String>
    var dropzoneText: String
    var filesLimit: Int
    var dropzoneClass: String
    var maxFileSile: Long
    var dropzoneParagraphClass: String
    var onAdd: (dynamic) -> Unit
    var onDelete: (dynamic) -> Unit
}

fun RBuilder.umDropZone(
    acceptedFiles: Array<String> = arrayOf("image/*"),
    clearOnUnmount: Boolean = true,
    showPreviews: Boolean = true,
    showFileNames: Boolean = true,
    showAlerts: Boolean = false,
    initialFiles: Array<dynamic> = arrayOf(),
    dropzoneText: String ? = null,
    filesLimit: Int = 1,
    onAdd: (dynamic) -> Unit = {},
    onDelete: (dynamic) -> Unit = {},
    dropzoneClass: String? = null,
    dropzoneParagraphClass: String? = null,
    className: String? = null,
    handler: StyledHandler<DropZoneProps>? = null
) = createStyledComponent(dropZoneComponent.unsafeCast<ComponentType<DropZoneProps>>(), className, handler) {
   attrs.acceptedFiles = acceptedFiles
    attrs.clearOnUnmount = clearOnUnmount
    attrs.showAlerts = showAlerts
    attrs.showPreviews = showPreviews
    attrs.showFileNames = showFileNames
    attrs.initialFiles = initialFiles
    attrs.maxFileSile = 671088640L
    dropzoneClass?.let { attrs.dropzoneClass = it }
    dropzoneParagraphClass?.let { attrs.dropzoneParagraphClass = it }
    dropzoneText?.let{ attrs.dropzoneText = it }
    attrs.filesLimit = filesLimit
    attrs.onAdd = onAdd
    attrs.onDelete = onDelete
}
