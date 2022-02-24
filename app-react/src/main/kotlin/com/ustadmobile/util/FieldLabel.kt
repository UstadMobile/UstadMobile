package com.ustadmobile.util

data class FieldLabel(
    var text: String? = null,
    var errorText: String? = null,
    var hint: String? = null,
    var id: String? = null,
){
    val error = errorText != null
    val width = (text?.length?:1) * 8
}
