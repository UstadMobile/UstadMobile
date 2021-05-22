package com.ustadmobile.model

data class UmLabel(var text: String? = null, var caption: String? = null){
    val width = if(text != null) text?.trim()?.length.toString().toInt() * 9 else 0
    val error = !caption.isNullOrEmpty()

}
