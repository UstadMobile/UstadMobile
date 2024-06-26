package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.ui.graphics.Color
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.theme.md_theme_dark_onError
import com.ustadmobile.libuicompose.theme.md_theme_dark_onErrorContainer
import com.ustadmobile.libuicompose.theme.onSuccessContainerDark
import com.ustadmobile.libuicompose.theme.successContainerDark

//Reserved for future use e.g when courseblock has its own thresholds
@Suppress("UnusedReceiverParameter")
fun CourseBlock.colorsForMark(scoredScaled: Float): Pair<Color, Color> {
    return when {
        scoredScaled >= 0.75f -> Pair(onSuccessContainerDark, successContainerDark)
        scoredScaled >= 0.5f -> Pair(Color.Black, Color.Yellow)//should be updated
        else -> Pair(md_theme_dark_onError, md_theme_dark_onErrorContainer)
    }
}
