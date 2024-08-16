package com.ustadmobile.core.viewmodel.coursegroupset.detail

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Person
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
                clazz.clazzUid = CreateNewClazzUseCase(activeDb).invoke(clazz)


                val courseGroupSet = CourseGroupSet().apply {
                    cgsName = "Assignment groups"
                    cgsClazzUid = clazz.clazzUid
                    cgsUid = activeDb.courseGroupSetDao().insertAsync(this)
                }

                studentNames.forEachIndexed { index, name ->
                    val person = Person().apply {
                        firstNames = name.substringBefore(" ")
                        lastName = name.substringAfter(" ")
                    }

                    person.personUid = AddNewPersonUseCase(activeDb, null).invoke(person)

                    activeDb.clazzEnrolmentDao().insertAsync(
                        ClazzEnrolment(
                            clazz.clazzUid, person.personUid, ClazzEnrolment.ROLE_STUDENT
                        )
                    )

                    activeDb.courseGroupMemberDao().upsertListAsync(listOf(
                        CourseGroupMember().apply {
                            cgmSetUid = courseGroupSet.cgsUid
                            cgmGroupNumber = index % 2
                            cgmPersonUid = person.personUid
                        }
                    ))
                }

                activeDb.coursePermissionDao().upsertAsync(
                    CoursePermission(
                        cpToPersonUid = activeUser.personUid,
                        cpClazzUid = clazz.clazzUid,
                        cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS
                    )
                )

                courseGroupSet
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = courseGroupSet.cgsUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = courseGroupSet.cgsClazzUid.toString()
                CourseGroupSetDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
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