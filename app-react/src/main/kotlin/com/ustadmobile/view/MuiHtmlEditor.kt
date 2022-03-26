package com.ustadmobile.view

import com.ustadmobile.mui.ext.createStyledComponent
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
    var toolbarButtonSize: dynamic
    var readOnly: Boolean
    var inheritFontSize: Boolean
    var onSave:(data: String) -> Unit
    var onChange:(data: dynamic) -> Unit
}

/**
 * Html editor with MUI component, this is equivalent to AztecText on android
 */
fun RBuilder.umMuiHtmlEditor(
    value: String?,
    readOnly: Boolean = false,
    onSave:((data: String) -> Unit)? = null,
    onChange:(data: String) -> Unit,
    className: String? = null,
    handler: StyledHandler<MuiEditorProps>? = null
) = createStyledComponent(muiHtmlEditorComponent.unsafeCast<ComponentType<MuiEditorProps>>(), className, handler) {
    attrs.defaultValue = convertDataToEditorState(value ?: "")
    attrs.toolbarButtonSize = "medium"
    attrs.inheritFontSize = true
    attrs.readOnly = readOnly
    onSave?.let{ attrs.onSave = it }
    attrs.onChange = {
        onChange.invoke(convertEditorContentToHtml(it.getCurrentContent()))
    }
}
