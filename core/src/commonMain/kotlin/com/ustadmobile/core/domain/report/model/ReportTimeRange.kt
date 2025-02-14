package com.ustadmobile.core.domain.report.model

import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import com.ustadmobile.core.MR

sealed class ReportTimeRange {

    abstract val from: Long

    abstract val to: Long

    abstract val label: StringResource?

}

class LastWeekReportTimeRange : ReportTimeRange() {

    override val from: Long
        get() = Clock.System.now().minus(7.days).toEpochMilliseconds()

    override val to: Long
        get() = Clock.System.now().toEpochMilliseconds()

    override val label: StringResource
        get() = MR.strings.last_week

}
