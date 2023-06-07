package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

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
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApproveOrDeclinePendingEnrolmentUseCaseTest {

    private lateinit var database: UmAppDatabase

    private lateinit var clazz: Clazz

    private lateinit var person: Person

    @BeforeTest
    fun setup() {
        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())

        database = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
            "jdbc:sqlite:build/tmp/approveordeclineusecase_${systemTimeInMillis()}.sqlite")
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)

        clazz = Clazz().apply {
            clazzName = "Test clazz"
        }

        val mockSystemImpl = mock<UstadMobileSystemImpl> {
            on { getString(any<Int>()) }.thenReturn("")
        }

        runBlocking {
            database.createNewClazzAndGroups(clazz, mockSystemImpl, emptyMap())
            person = database.insertPersonAndGroup(Person().apply {
                firstNames = "Test"
                lastName = "User"
            })
        }
    }

    @Test
    fun givenPendingEnrolment_whenApproved_thenClazzEnrolmentRoleIsStudentAndIsInStudentPersonGroup() {
        runBlocking {
            database.enrolPersonIntoClazzAtLocalTimezone(
                person, clazz.clazzUid, ClazzEnrolment.ROLE_STUDENT_PENDING)

            val useCase = ApproveOrDeclinePendingEnrolmentUseCase(database)
            useCase(
                personUid = person.personUid,
                clazzUid = clazz.clazzUid,
                approved = true
            )

            val inClazzGroup = database.personGroupMemberDao.findAllGroupWherePersonIsIn(person.personUid).any {
                it.groupMemberGroupUid == clazz.clazzStudentsPersonGroupUid
            }

            assertTrue(inClazzGroup)

            val enrolmentInDb = database.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(
                person.personUid, clazz.clazzUid
            )

            assertEquals(ClazzEnrolment.ROLE_STUDENT, enrolmentInDb?.clazzEnrolmentRole)
        }
    }

    @Test
    fun givenPendingEnrolment_whenDeclined_thenGroupMembershipAndPendingEnrolmentIsSetToInactive() {
        runBlocking {
            database.enrolPersonIntoClazzAtLocalTimezone(
                person, clazz.clazzUid, ClazzEnrolment.ROLE_STUDENT_PENDING
            )

            val useCase = ApproveOrDeclinePendingEnrolmentUseCase(database)
            useCase(
                personUid = person.personUid,
                clazzUid = clazz.clazzUid,
                approved = false
            )

            val inClazzGroup = database.personGroupMemberDao.findAllGroupWherePersonIsIn(person.personUid).any {
                it.groupMemberGroupUid == clazz.clazzStudentsPersonGroupUid ||
                    it.groupMemberGroupUid == clazz.clazzPendingStudentsPersonGroupUid
            }

            assertFalse(inClazzGroup)

            val activeEnrolments = database.clazzEnrolmentDao.findAllClazzesByPersonWithClazzAsListAsync(
                person.personUid
            )

            assertTrue(activeEnrolments.isEmpty())
        }
    }

}