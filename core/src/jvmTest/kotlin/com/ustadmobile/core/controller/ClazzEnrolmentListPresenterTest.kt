
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class ClazzEnrolmentListPresenterTest {

    private lateinit var activePerson: Person
    private lateinit var testClazz: Clazz

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzEnrolmentListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzEnrolmentDaoSpy: ClazzEnrolmentDao

    private lateinit var di: DI

    private lateinit var mockNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)

        di = DI {
            import(ustadTestRule.diModule)
        }
        val repo: UmAppDatabase by di.activeRepoInstance()
        mockNavController = di.direct.instance()
        context = Any()
        repoClazzEnrolmentDaoSpy = spy(repo.clazzEnrolmentDao)
        whenever(repo.clazzEnrolmentDao).thenReturn(repoClazzEnrolmentDaoSpy)

        testClazz = Clazz("Test clazz").apply {
            clazzUid = repo.clazzDao.insert(this)
        }

        activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }

    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = ClazzEnrolmentWithLeavingReason().apply {
            //set variables here
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>(
                ARG_PERSON_UID to activePerson.personUid.toString(),
                ARG_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzEnrolmentListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzEnrolmentDaoSpy, timeout(5000)).findAllEnrolmentsByPersonAndClazzUid(
                eq(activePerson.personUid),eq(testClazz.clazzUid))
        verify(mockView, timeout(5000)).enrolmentList = any()

    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToEditView() {

        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = ClazzEnrolmentWithLeavingReason().apply {
            //set variables here
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>(
                ARG_PERSON_UID to activePerson.personUid.toString(),
                ARG_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzEnrolmentListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        verify(mockView, timeout(5000L).atLeastOnce()).enrolmentList = any()

        presenter.handleClickClazzEnrolment(testEntity)

        verify(mockNavController, timeout(5000)).navigate(
            eq(ClazzEnrolmentEditView.VIEW_NAME),
            any(), any()
        )

    }

}
