package com.ustadmobile.view.clazz.gradebook

import com.ustadmobile.lib.db.entities.CourseBlock
import mui.material.styles.PaletteColor
import mui.material.styles.Theme

//Reserved for future use e.g when courseblock has its own thresholds
@Suppress("UnusedReceiverParameter")
fun CourseBlock.colorForMark(
    theme: Theme,
    scoreScaled: Float
): PaletteColor {
    return when {
        scoreScaled >= 0.75f -> theme.palette.success
        scoreScaled >= 0.5f -> theme.palette.warning
        else -> theme.palette.error
    }
}
