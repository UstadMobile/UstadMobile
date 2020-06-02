
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Clazz
import org.junit.Assert

class ClazzList2PresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzList2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzDaoSpy = spy(clientDbRule.db.clazzDao)
        whenever(clientDbRule.db.clazzDao).thenReturn(repoClazzDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = clientDbRule.db.clazzDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // ClazzList2View.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = ClazzList2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzDaoSpy, timeout(5000)).findAllActiveClazzesSortByNameAsc(
                eq("%"), eq(clientDbRule.account.personUid), eq(0))
        verify(mockView, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    //Example Note: It is NOT required to have separate tests for filters when they are all simply passed to the same DAO method
    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalledWithExcludeArgs_thenShouldQueryDatabaseAndSetOnView() {
        val excludeFromSchool = 7L
        val presenterArgs = mapOf<String,String>(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to excludeFromSchool.toString())
        val presenter = ClazzList2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzDaoSpy, timeout(5000)).findAllActiveClazzesSortByNameAsc(
                eq("%"), eq(clientDbRule.account.personUid), eq(excludeFromSchool))
        verify(mockView, timeout(5000)).list = any()
    }


    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = clientDbRule.db.clazzDao.insert(this)
        }
        val presenter = ClazzList2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ClazzDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())), any())
    }


}