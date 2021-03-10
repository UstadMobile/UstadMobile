
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZWORK_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SAVE_TO_DB
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

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

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzEnrolmentDaoSpy: ClazzEnrolmentDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        di = DI {
            import(ustadTestRule.diModule)
        }
        val repo: UmAppDatabase by di.activeRepoInstance()
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
                ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString())
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
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val testEntity = ClazzEnrolmentWithLeavingReason().apply {
            //set variables here
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>(
                ARG_PERSON_UID to activePerson.personUid.toString(),
                ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzEnrolmentListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        verify(mockView, timeout(5000L).atLeastOnce()).enrolmentList = any()

        presenter.handleClickClazzEnrolment(testEntity)

        verify(systemImpl, timeout(5000)).go(eq(ClazzEnrolmentEditView.VIEW_NAME),
              any(), any())

    }

}
