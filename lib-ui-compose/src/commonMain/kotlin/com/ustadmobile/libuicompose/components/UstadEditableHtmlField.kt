package com.ustadmobile.libuicompose.components

expect fun UstadEditableHtmlField(

    html: String,

    onHtmlChange: () -> String,

    onClickToEditInNewScreen: () -> Unit,

)