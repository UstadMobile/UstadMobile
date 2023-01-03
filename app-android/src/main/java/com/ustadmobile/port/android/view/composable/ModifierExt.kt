package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.paddingCourseBlockIndent(
    indentLevel: Int
) = this.padding(start =
((indentLevel ?: 0) * 24).dp
)