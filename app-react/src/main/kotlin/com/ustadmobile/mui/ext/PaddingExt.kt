package com.ustadmobile.mui.ext

import csstype.Padding
import csstype.px
import mui.material.GridProps

fun Padding.paddingCourseBlockIndent(
    indentLevel: Int?
): Padding {
    return Padding(
        left = ((indentLevel ?: 0) * 24).px,
        right = 10.px,
        bottom = 10.px,
        top = 10.px
    )
}