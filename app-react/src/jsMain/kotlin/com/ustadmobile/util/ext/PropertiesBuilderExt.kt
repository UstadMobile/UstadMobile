package com.ustadmobile.util.ext

import com.ustadmobile.mui.common.DisplayWebkitBox
import com.ustadmobile.mui.common.webKitLineClamp
import com.ustadmobile.mui.common.webkitBoxOrient
import csstype.PropertiesBuilder
import web.cssom.Overflow
import web.cssom.TextOverflow

fun PropertiesBuilder.useLineClamp(numLines: Int) {
    webKitLineClamp = numLines
    display = DisplayWebkitBox
    webkitBoxOrient = "vertical"
    overflow = Overflow.hidden
    textOverflow = TextOverflow.ellipsis
}
