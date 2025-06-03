package com.ustadmobile.core.viewmodel.coursegroupset.list

import app.cash.turbine.test
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CourseGroupSetListViewModelTest : AbstractMainDispatcherTest() {

    @Test
    fun givenExistingCourseGroupSetAndUserHasPermissionToAdd_whenInitiated_thenWillShowExistingGroupSetAndAddOption() {
        testViewModel<CourseGroupSetListViewModel> {
            val activeUser = setActiveUser(LearningSpace("https://test.com/"))

            val clazzUid = activeDb.withDoorTransactionAsync {
                val clazz = Clazz().apply {
                    clazzName = "Group Course"
                }
                clazz.clazzUid = CreateNewClazzUseCase(activeDb).invoke(clazz)

                activeDb.coursePermissionDao().upsertAsync(
                    CoursePermission(
                        cpToPersonUid = activeUser.personUid,
                        cpClazzUid =  clazz.clazzUid,
                        cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS
                    )
                )

                activeDb.courseGroupSetDao().insertAsync(CourseGroupSet().apply {
                    cgsName = "Assignment groups"
                    cgsClazzUid = clazz.clazzUid
                })

                clazz.clazzUid
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = clazzUid.toString()
                CourseGroupSetListViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.courseGroupSets() !is EmptyPagingSource }
                val groupSets = readyState.courseGroupSets().loadFirstList()
                assertEquals("Assignment groups", groupSets.first().cgsName)


                viewModel.appUiState.assertItemReceived(timeout = 500.seconds) {
                    it.fabState.visible
                }

                cancelAndIgnoreRemainingEvents()
            }


        }
    }


}