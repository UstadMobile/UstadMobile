package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestEnrolmentUseCaseJvmTest {

    private lateinit var database: UmAppDatabase

    private lateinit var requesterPerson: Person

    @BeforeTest
    fun setup() {
        database = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()

        runBlocking {
            requesterPerson = database.insertPersonAndGroup(Person().apply {
                firstNames = "Student"
                lastName = "Applicant"
                username = "student"
            })
        }
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    inline fun assertBlockThrows(
        expectedException: (Throwable) -> Boolean,
        message: String? = null,
        block: () -> Unit,
    ) {
        try {
            block()
            throw IllegalStateException("Assert throws: nothing thrown")
        }catch(e: Throwable) {
            assertTrue(expectedException(e),
                message ?: "Throwable should match expected exception")
        }
    }


    @Test
    fun givenInvalidCode_whenInvoked_thenWillThrowIllegalArgException() {
        assertBlockThrows(
            expectedException = { it is IllegalArgumentException }
        ) {
            val useCase = RequestEnrolmentUseCase(database)
            runBlocking {
                useCase("wrong", requesterPerson, ClazzEnrolment.ROLE_STUDENT)
            }
        }
    }

    @Test
    fun givenPendingRequestAlreadyExists_whenInvoked_thenWillThrowAlreadyHasPendingEnrolmentException(
    )= assertBlockThrows(
        expectedException = {
            it is AlreadyHasPendingRequestException
        }
    ) {
        val code = "aa11"
        runBlocking {
            val clazzUid = database.clazzDao().insert(
                Clazz().apply{
                    clazzName = "Test"
                    clazzCode = code
                }
            )

            database.enrolmentRequestDao().insert(
                EnrolmentRequest(
                    erClazzUid = clazzUid,
                    erPersonUid = requesterPerson.personUid
                )
            )

            RequestEnrolmentUseCase(database).invoke(
                code, requesterPerson, ClazzEnrolment.ROLE_STUDENT
            )
        }
    }

    @Test
    fun givenPersonAlreadyEnroled_whenInvoked_thenWillThrowAlreadyEnroledException(

    ) =assertBlockThrows(
        expectedException = { it is AlreadyEnroledInClassException }
    ){
        val code = "1122"
        runBlocking {
            val clazzUid = database.clazzDao().insert(
                Clazz().apply{
                    clazzName = "Test"
                    clazzCode = code
                }
            )
            database.clazzEnrolmentDao().insertAsync(
                ClazzEnrolment(
                    clazzUid = clazzUid,
                    personUid = requesterPerson.personUid,
                    role = ClazzEnrolment.ROLE_STUDENT
                ).apply {
                    clazzEnrolmentDateJoined = systemTimeInMillis()
                    clazzEnrolmentDateLeft = UNSET_DISTANT_FUTURE
                }
            )

            RequestEnrolmentUseCase(database).invoke(
                code, requesterPerson, ClazzEnrolment.ROLE_STUDENT
            )
        }
    }

    @Test
    fun givenNoPendingEnrolmentsOrRequests_whenInvoked_thenRequestInserted() {
        runBlocking {
            val code = "aabb"
            val clazzUid = database.clazzDao().insert(
                Clazz().apply {
                    clazzName = "Test"
                    clazzCode = code
                }
            )

            RequestEnrolmentUseCase(database).invoke(
                code, requesterPerson, ClazzEnrolment.ROLE_STUDENT
            )

            val pendingRequest = database.enrolmentRequestDao().findByClazzAndPerson(
                requesterPerson.personUid, clazzUid, 0
            )

            assertEquals(EnrolmentRequest.STATUS_PENDING, pendingRequest.first().erStatus)
            assertEquals(requesterPerson.fullName(), pendingRequest.first().erPersonFullname)
        }
    }

}