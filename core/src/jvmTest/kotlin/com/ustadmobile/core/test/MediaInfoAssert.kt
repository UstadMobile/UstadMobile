package com.ustadmobile.core.test

import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.duration
import java.io.File
import kotlin.math.abs
import kotlin.test.assertTrue

fun assertSameMediaDuration(
    execMediaInfoUseCase: ExecuteMediaInfoUseCase,
    expectedDurationFile: File,
    actualDurationFile: File,
    tolerance: Int = 500,
) {
    val expectedMediaInfo = execMediaInfoUseCase(expectedDurationFile)
    val actualMediaInfo = execMediaInfoUseCase(actualDurationFile)

    assertTrue(abs(expectedMediaInfo.duration() - actualMediaInfo.duration()) < tolerance)
}
