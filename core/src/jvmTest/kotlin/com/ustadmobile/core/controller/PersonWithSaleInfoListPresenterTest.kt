
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.PersonWithSaleInfoDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class PersonWithSaleInfoListPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: PersonWithSaleInfoListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoPersonWithSaleInfoDaoSpy: PersonWithSaleInfoDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoPersonWithSaleInfoDaoSpy = spy(clientDbRule.db.personWithSaleInfoDao)
        whenever(clientDbRule.db.personWithSaleInfoDao).thenReturn(repoPersonWithSaleInfoDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val testEntity = PersonWithSaleInfo().apply {
            //set variables here
            personWithSaleInfoUid = clientDbRule.db.personWithSaleInfoDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // PersonWithSaleInfoListView.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = PersonWithSaleInfoListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoPersonWithSaleInfoDaoSpy, timeout(5000)).findByPersonWithSaleInfoUidAsFactory()
        verify(mockView, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val testEntity = PersonWithSaleInfo().apply {
            //set variables here
            personWithSaleInfoUid = clientDbRule.db.personWithSaleInfoDao.insert(this)
        }
        val presenter = PersonWithSaleInfoListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)


        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(PersonWithSaleInfoDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.personWithSaleInfoUid.toString())), any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}