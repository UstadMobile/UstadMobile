package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.lib.db.entities.Person

class RequestEnrolmentUseCase(
    private val activeRepo: UmAppDatabase,
) {


    /**
     * Request an enrolment into given course using the code.
     *
     * @param clazzCode the clazzCode for course that the person wishes to enrol into
     * @param person the person requesting the enrolment
     * @param roleId the role being requested
     */
    suspend operator fun invoke(
        clazzCode: String,
        person: Person,
        roleId: Int,
    ) {
        val clazz = activeRepo.clazzDao().findByClazzCode(clazzCode)
            ?: throw IllegalArgumentException()

        //check for already existing requests
        if(
            activeRepo.enrolmentRequestDao().hasPendingRequests(
                personUid = person.personUid,
                clazzUid = clazz.clazzUid
            )
        ) {
            throw AlreadyHasPendingRequestException()
        }

        if(
            activeRepo.clazzEnrolmentDao().getAllEnrolmentsAtTimeByClazzAndPerson(
                clazzUid = clazz.clazzUid,
                accountPersonUid = person.personUid,
                time = systemTimeInMillis()
            ).isNotEmpty()
        ) {
            throw AlreadyEnroledInClassException()
        }


        activeRepo.enrolmentRequestDao().insert(
            EnrolmentRequest(
                erClazzUid = clazz.clazzUid,
                erClazzName = clazz.clazzName,
                erPersonUid = person.personUid,
                erPersonFullname = person.fullName(),
                erPersonUsername = person.username,
                erRole = roleId,
                erRequestTime = systemTimeInMillis(),
                erStatus = EnrolmentRequest.STATUS_PENDING,
            )
        )
    }

}