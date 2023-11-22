package com.ustadmobile.libuicompose.view.clazz

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.paddingCourseBlockIndent(
    indentLevel: Int
) = this.padding(start =
   (indentLevel * 24).dp
)