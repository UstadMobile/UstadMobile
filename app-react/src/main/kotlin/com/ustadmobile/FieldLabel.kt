package com.ustadmobile

data class FieldLabel(var text: String? = null,
                      var errorText: String? = null,
                      var id: String? = null,){
    val error = !errorText.isNullOrEmpty()
    val width = (text?.length?:1) * 8
}
