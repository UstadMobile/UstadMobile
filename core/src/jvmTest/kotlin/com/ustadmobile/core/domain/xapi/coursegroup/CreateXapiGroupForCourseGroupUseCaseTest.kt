package com.ustadmobile.core.domain.xapi.coursegroup

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateXapiGroupForCourseGroupUseCaseTest {

    private lateinit var db: UmAppDatabase

    private lateinit var stringHasher: XXStringHasher

    private val endpoint = Endpoint("http://example.com/")

    @BeforeTest
    fun setup() {
        stringHasher = XXStringHasherCommonJvm()
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        )
        .build()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun givenValidCourseGroupSet_whenInvoked_thenXapiGroupCreated() {
        runBlocking {
            val clazz = Clazz().apply {
                clazzUid = 1L
                clazzName = "Test clazz"
            }
            CreateNewClazzUseCase(db).invoke(clazz)
            val courseGroupSet = CourseGroupSet().apply {
                cgsClazzUid = clazz.clazzUid
                cgsUid = 50L
            }
            db.courseGroupSetDao.insertAsync(courseGroupSet)

            val enrolUseCase = EnrolIntoCourseUseCase(db, null)

            val teacherPerson = Person().apply {
                personUid = 150L
                firstNames = "Edna"
                lastName = "K"
                username = "ednak"
            }
            db.personDao.insertAsync(teacherPerson)
            enrolUseCase(
                enrolment = ClazzEnrolment(
                    clazzUid = clazz.clazzUid,
                    personUid = teacherPerson.personUid,
                ).apply {
                    clazzEnrolmentRole = ClazzEnrolment.ROLE_TEACHER
                },
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            val studentPersons = db.withDoorTransactionAsync {
                (1..10).map {
                    val studentPerson = Person().apply {
                        personUid = it.toLong()
                        firstNames = "Test Person"
                        lastName = "$it"
                        username = "testperson$it"
                    }
                    db.personDao.insertAsync(studentPerson)

                    enrolUseCase(
                        enrolment = ClazzEnrolment().apply {
                            clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                            clazzEnrolmentClazzUid = clazz.clazzUid
                            clazzEnrolmentPersonUid = studentPerson.personUid
                        },
                        timeZoneId = TimeZone.currentSystemDefault().id,
                    )

                    val courseGroupMember = CourseGroupMember(
                        cgmGroupNumber = it.mod(3) + 1,
                        cgmSetUid = courseGroupSet.cgsUid,
                        cgmPersonUid = studentPerson.personUid
                    )
                    db.courseGroupMemberDao.insertAsync(courseGroupMember)

                    studentPerson to courseGroupMember
                }
            }

            val assignment = ClazzAssignment(
                caUid = 100L,
                caClazzUid = clazz.clazzUid,
            )
            db.clazzAssignmentDao.insertAsync(assignment)

            val useCase = CreateXapiGroupForCourseGroupUseCase(db, endpoint, stringHasher)
            val result = useCase(
                groupSetUid = courseGroupSet.cgsUid,
                groupNum = 1,
                clazzUid = clazz.clazzUid,
                assignmentUid = assignment.caUid,
                accountPersonUid =teacherPerson.personUid
            )
            assertEquals(
                studentPersons.map { it.second }.count { it.cgmGroupNumber == 1 },
                result.group.member.size
            )
        }
    }


}