package com.ustadmobile.core.viewmodel.coursegroupset.list

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CourseGroupSetListViewModelTest {

    @Test
    fun givenExistingCourseGroupSetAndUserHasPermissionToAdd_whenInitiated_thenWillShowExistingGroupSetAndAddOption() {
        testViewModel<CourseGroupSetListViewModel> {
            val activeUser = setActiveUser(Endpoint("https://test.com/"))

            val clazzUid = activeDb.withDoorTransactionAsync {
                val clazz = Clazz().apply {
                    clazzName = "Group Course"
                }
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())
                activeDb.grantScopedPermission(
                    toPerson = activeUser,
                    permissions = Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT,
                    scopeTableId = Clazz.TABLE_ID,
                    scopeEntityUid = clazz.clazzUid
                )

                activeDb.courseGroupSetDao.insertAsync(CourseGroupSet().apply {
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