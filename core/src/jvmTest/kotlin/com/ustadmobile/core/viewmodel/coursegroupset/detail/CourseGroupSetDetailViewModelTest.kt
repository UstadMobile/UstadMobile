package com.ustadmobile.core.viewmodel.coursegroupset.detail

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CourseGroupSetDetailViewModelTest : AbstractMainDispatcherTest()  {

    val endpoint = Endpoint("https://www.test.com/")

    @Test
    fun givenExistingCourseGroupSetWithMembers_whenInitiated_thenWillShowMembersListAndEditButton() {
        testViewModel<CourseGroupSetDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            val studentNames = listOf(
                "Bart Simpson",
                "Nelzon Muntz",
                "Sherry Mackleberry",
                "Terry Mackleberry",
                "Milhouse VanHouten"
            )

            val courseGroupSet = activeDb.withDoorTransactionAsync {
                val clazz = Clazz().apply {
                    clazzName = "test clazz"
                }
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())


                val courseGroupSet = CourseGroupSet().apply {
                    cgsName = "Assignment groups"
                    cgsClazzUid = clazz.clazzUid
                    cgsUid = activeDb.courseGroupSetDao.insertAsync(this)
                }

                studentNames.forEachIndexed { index, name ->
                    val person = activeDb.insertPersonAndGroup(Person().apply {
                        firstNames = name.substringBefore(" ")
                        lastName = name.substringAfter(" ")
                    })
                    activeDb.enrolPersonIntoClazzAtLocalTimezone(person, clazz.clazzUid,
                        ClazzEnrolment.ROLE_STUDENT)
                    activeDb.courseGroupMemberDao.upsertListAsync(listOf(
                        CourseGroupMember().apply {
                            cgmSetUid = courseGroupSet.cgsUid
                            cgmGroupNumber = index % 2
                            cgmPersonUid = person.personUid
                        }
                    ))
                }

                activeDb.grantScopedPermission(activeUser,
                    Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, Clazz.TABLE_ID,
                    clazz.clazzUid)

                courseGroupSet
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = courseGroupSet.cgsUid.toString()
                CourseGroupSetDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 500.seconds) {
                val readyState = awaitItemWhere {
                    it.courseGroupSet != null && it.membersList.isNotEmpty()
                }

                assertEquals("Assignment groups", readyState.courseGroupSet?.cgsName)
                assertEquals(studentNames.size, readyState.membersList.size)
                studentNames.forEach { name ->
                    assertTrue(readyState.membersList.any { it.name == name} )
                }
            }
        }
    }

}