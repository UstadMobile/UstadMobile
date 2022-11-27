package com.ustadmobile.view

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.DraftJsUtil.convertDataToEditorState
import com.ustadmobile.util.DraftJsUtil.convertEditorContentToHtml
import com.ustadmobile.util.UmState
import react.ComponentType
import react.RBuilder
import react.RComponent
import styled.StyledHandler
import styled.StyledProps

@JsModule("mui-rte")
@JsNonModule
private external val muiHtmlEditorModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val muiHtmlEditorComponent: RComponent<MuiEditorProps, UmState> = muiHtmlEditorModule.default


external interface MuiEditorProps: StyledProps {
    var defaultValue: String
    var id: String
    var label: String
    var toolbarButtonSize: dynamic
    var readOnly: Boolean
    var toolbar: Boolean
    var inheritFontSize: Boolean
    var maxLength: Int
    var onSave:(data: String) -> Unit
    var onChange:(data: dynamic) -> Unit
}

/**
 * Html editor with MUI component, this is equivalent to AztecText on android
 */
fun RBuilder.umMuiHtmlEditor(
    value: String?,
    readOnly: Boolean = false,
    toolbar: Boolean = true,
    label: String? = null,
    onSave:((data: String) -> Unit)? = null,
    onChange:((data: String) -> Unit)? = null,
    className: String? = null,
    maxLength: Int? = null,
    handler: StyledHandler<MuiEditorProps>? = null
) = convertFunctionalToClassElement(muiHtmlEditorComponent.unsafeCast<ComponentType<MuiEditorProps>>(), className, handler) {
    attrs.defaultValue = convertDataToEditorState(value ?: "")
    attrs.toolbarButtonSize = "medium"
    attrs.inheritFontSize = true
    attrs.readOnly = readOnly
    attrs.id = "um-html-editor"
    onSave?.let{ attrs.onSave = it }
    maxLength?.let{
        attrs.maxLength = it
    }
    label?.let{
        attrs.label = it
    }
    attrs.toolbar = toolbar
    attrs.onChange = {
        onChange?.invoke(convertEditorContentToHtml(it.getCurrentContent()))
    }
}
