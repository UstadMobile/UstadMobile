package com.ustadmobile.core.domain.assignment.submitassignment

import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.nio.file.Files
import kotlin.random.Random
import kotlin.test.assertEquals

class SubmitAssignmentUseCaseTest {

    private class SubmitUseCaseContext(
        val db: UmAppDatabase,
        val systemImpl: UstadMobileSystemImpl,
        val assignment: ClazzAssignment,
        val courseBlock: CourseBlock,
        val person: Person,
    )


    private fun testSubmitAssignment(
        accountPersonUidRole: Int = ClazzEnrolment.ROLE_STUDENT,
        assignment: ClazzAssignment = ClazzAssignment(),
        courseBlock: CourseBlock = CourseBlock(),
        block: suspend SubmitUseCaseContext.() -> Unit,
    ) {
        val tmpDir = Files.createTempDirectory("submittest").toFile()

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
        val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
            "jdbc:sqlite:build/tmp/submitusecase_${systemTimeInMillis()}.sqlite", nodeId = nodeIdAndAuth.nodeId)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()
        val systemImpl: UstadMobileSystemImpl = mock {
            on { getString(any())}.thenAnswer { it.arguments.first().toString() }
        }

        try {
            val clazz = Clazz().apply {
                clazzName = "test clazz"
            }
            val person = Person().apply {
                firstNames = "Test"
                lastName = "user"
            }

            courseBlock.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE

            runBlocking {
                db.withDoorTransactionAsync {
                    db.createNewClazzAndGroups(clazz, systemImpl, emptyMap())
                    db.insertPersonAndGroup(person)
                    if(accountPersonUidRole != 0) {
                        db.enrolPersonIntoClazzAtLocalTimezone(
                            person, clazz.clazzUid, accountPersonUidRole,
                        )
                    }

                    assignment.caClazzUid = clazz.clazzUid
                    assignment.caUid = db.clazzAssignmentDao.insertAsync(assignment)
                    courseBlock.cbEntityUid = assignment.caUid
                    courseBlock.cbUid = db.courseBlockDao.insertAsync(courseBlock)
                }
                block(SubmitUseCaseContext(db, systemImpl, assignment, courseBlock, person))
            }
        }finally {
            tmpDir.deleteRecursively()
            db.close()
        }
    }

    @Test
    fun givenValidAssignment_whenNoSubmissionMadeYet_thenWillSaveSubmission() {
        testSubmitAssignment {
            val submitUseCase = SubmitAssignmentUseCase()
            val response = "I can has cheezburger"
            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = response
                }
            )

            val submissionInDb = db.courseAssignmentSubmissionDao.getLatestSubmissionForUserAsync(
                accountPersonUid = person.personUid, assignmentUid = assignment.caUid
            )

            assertEquals(response, submissionInDb?.casText)
        }
    }

    @Test(expected = AssignmentAlreadySubmittedException::class)
    fun givenValidAssignment_whenSubmissionAlreadyMadeAndPolicyIsSubmitAllAtOnce_thenWillThrowAlreadySubmittedException() {
        testSubmitAssignment(
            assignment = ClazzAssignment().apply {
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            }
        ) {
            db.courseAssignmentSubmissionDao.insertAsync(CourseAssignmentSubmission().apply {
                casAssignmentUid = assignment.caUid
                casSubmitterUid = person.personUid
                casText = "Burger!"
            })

            val submitUseCase = SubmitAssignmentUseCase()
            val response = "I can has cheezburger"

            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = response
                }
            )
        }
    }

    @Test(expected = AccountIsNotSubmitterException::class)
    fun givenValidAssignment_whenAccountPersonUidIsNotSubmitter_thenWillThrowNotSubmitterException() {
        testSubmitAssignment(
            accountPersonUidRole = 0
        ) {
            val submitUseCase = SubmitAssignmentUseCase()

            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = "Burger!"
                }
            )
        }
    }

    @Test
    fun givenValidAssignment_whenSubmissionAlreadyMadeAndPolicyIsAllowMultipleSubmissions_thenWillSaveSubmission() {
        testSubmitAssignment(
            assignment = ClazzAssignment().apply {
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED
            }
        ) {
            db.courseAssignmentSubmissionDao.insertAsync(CourseAssignmentSubmission().apply {
                casAssignmentUid = assignment.caUid
                casSubmitterUid = person.personUid
                casText = "Burger!"
            })

            val submitUseCase = SubmitAssignmentUseCase()
            val response = "I can has cheezburger"

            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = response
                }
            )

            val submissionInDb = db.courseAssignmentSubmissionDao.getLatestSubmissionForUserAsync(
                accountPersonUid = person.personUid, assignmentUid = assignment.caUid
            )

            assertEquals(response, submissionInDb?.casText)
        }
    }


    @Test(expected = AssignmentDeadlinePassedException::class)
    fun givenValidAssignment_whenDeadlineAndGracePeriodPassed_thenWillThrowDeadlinePassedException() {
        testSubmitAssignment(
            courseBlock = CourseBlock().apply {
                cbDeadlineDate = systemTimeInMillis() - 10000
                cbGracePeriodDate = systemTimeInMillis() - 1000
            }
        ) {
            val submitUseCase = SubmitAssignmentUseCase()
            val response = "I can still has cheezburger"
            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = response
                }
            )
        }
    }

    @Test(expected = AssignmentTextTooLongException::class)
    fun givenValidAssignment_whenTextTooLong_thenWillThrowTextTooLongException() {
        testSubmitAssignment(
            assignment = ClazzAssignment().apply {
                caTextLimitType = ClazzAssignment.TEXT_WORD_LIMIT
                caTextLimit = 5
            }
        ) {
            val submitUseCase = SubmitAssignmentUseCase()
            val response = "I can still has cheezburger, right?"
            submitUseCase(
                db = db,
                systemImpl = systemImpl,
                assignmentUid = assignment.caUid,
                accountPersonUid = person.personUid,
                submission = CourseAssignmentSubmission().apply {
                    casText = response
                }
            )
        }
    }

}