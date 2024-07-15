package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.toLocalEndOfDay
import com.ustadmobile.core.util.ext.toLocalMidnight
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlinx.datetime.Instant

/**
 * Handle enrolling a person into a course. This will insert the Enrolment entity. It will set the
 * start date and end dates to use local midnight start of day and end of day respectively.
 */
class EnrolIntoCourseUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    /**
     * Process the given enrolment.
     */
    suspend operator fun invoke(
        enrolment: ClazzEnrolment,
        timeZoneId: String,
    ) : Long {
        enrolment.clazzEnrolmentDateJoined = Instant
            .fromEpochMilliseconds(enrolment.clazzEnrolmentDateJoined)
            .toLocalMidnight(timeZoneId).toEpochMilliseconds()

        if(enrolment.clazzEnrolmentDateLeft < UNSET_DISTANT_FUTURE){
            enrolment.clazzEnrolmentDateLeft = Instant
                .fromEpochMilliseconds(enrolment.clazzEnrolmentDateLeft)
                .toLocalEndOfDay(timeZoneId).toEpochMilliseconds()
        }

        val effectiveDb = (repo ?: db)

        return effectiveDb.withDoorTransactionAsync {
            effectiveDb.clazzEnrolmentDao().insertAsync(enrolment)
        }
    }

}