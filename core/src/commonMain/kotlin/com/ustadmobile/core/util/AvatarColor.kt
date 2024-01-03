package com.ustadmobile.core.util

//As per https://mui.com/material-ui/react-avatar/#letter-avatars
fun avatarColorForName(name: String): Int {
    var hash = 0
    name.forEach {
        hash = it.code + ((hash shl 5) - hash)
    }

    val colorInt = hash or 0xff
    return colorInt

}
