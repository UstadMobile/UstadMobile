package com.ustadmobile.mui.ext

import csstype.Padding
import csstype.px
import mui.material.GridProps

fun paddingCourseBlockIndent(
    indentLevel: Int
): Padding {
    return Padding(
        left = (16+((indentLevel) * 24)).px,
        right = 0.px,
        bottom = 0.px,
        top = 0.px
    )
}