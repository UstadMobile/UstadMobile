package com.ustadmobile.model

data class UmLabel(var text: String? = null, var error: Boolean = false){
    val width = if(text != null) text?.trim()?.length.toString().toInt() * 9 else 0

}
