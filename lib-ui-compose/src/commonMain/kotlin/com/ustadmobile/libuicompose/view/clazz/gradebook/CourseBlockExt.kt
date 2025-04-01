package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.ui.graphics.Color
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.theme.onErrorContainerDark
import com.ustadmobile.libuicompose.theme.onErrorDark
import com.ustadmobile.libuicompose.theme.onPrimaryContainerDark
import com.ustadmobile.libuicompose.theme.primaryLightHighContrast

//Reserved for future use e.g when courseblock has its own thresholds
@Suppress("UnusedReceiverParameter")
fun CourseBlock.colorsForMark(scoredScaled: Float): Pair<Color, Color> {
    return when {
        scoredScaled >= 0.75f -> Pair(onPrimaryContainerDark, primaryLightHighContrast)
        scoredScaled >= 0.5f -> Pair(Color.Black, Color.Yellow)//should be updated
        else -> Pair(onErrorDark, onErrorContainerDark)
    }
}
