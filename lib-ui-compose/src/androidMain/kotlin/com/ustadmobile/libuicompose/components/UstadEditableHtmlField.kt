package com.ustadmobile.libuicompose.components

actual fun UstadEditableHtmlField(
    html: String,
    onHtmlChange: () -> String,
    onClickToEditInNewScreen: () -> Unit
) {

    HtmlClickableTextField(
        html = html,
        label = "",
        onClick = onClickToEditInNewScreen
    )
}