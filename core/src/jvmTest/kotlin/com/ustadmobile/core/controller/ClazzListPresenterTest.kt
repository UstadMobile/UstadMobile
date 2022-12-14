
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzList2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoClazzDaoSpy = spy(repo.clazzDao)
        whenever(repo.clazzDao).thenReturn(repoClazzDaoSpy)


    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = repo.clazzDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = ClazzListPresenter(context, presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzDaoSpy, timeout(5000)).findClazzesWithPermission(
                eq("%"), eq(accountManager.activeAccount.personUid), eq(listOf()), eq(0),
                any(), any(), any(), any(), any())
        verify(mockView, timeout(5000)).list = any()

    }

    //Example Note: It is NOT required to have separate tests for filters when they are all simply passed to the same DAO method
    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalledWithExcludeArgs_thenShouldQueryDatabaseAndSetOnView() {
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        val excludeFromSchool = 7L
        val presenterArgs = mapOf(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to excludeFromSchool.toString())
        val presenter = ClazzListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzDaoSpy, timeout(5000)).findClazzesWithPermission(
                eq("%"), eq(accountManager.activeAccount.personUid), eq(listOf()),
                eq(excludeFromSchool), any(), any(), any(), any(), any()
        )
        verify(mockView, timeout(5000)).list = any()
    }


    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val systemImpl: UstadMobileSystemImpl by di.instance()

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid
        }

        val presenterArgs = mapOf<String,String>()
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = repo.clazzDao.insert(this)
        }

        runBlocking {
            repo.grantScopedPermission(activePerson, Role.PERMISSION_CLAZZ_OPEN,
                Clazz.TABLE_ID, testEntity.clazzUid)
        }

        val accountManager = di.direct.instance<UstadAccountManager>()
        val endpointUrl = accountManager.activeEndpoint.url
        accountManager.startLocalTestSessionBlocking(activePerson, endpointUrl)

        val presenter = ClazzListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.onClickClazz(testEntity)

        verify(systemImpl, timeout(5000)).go(eq(ClazzDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())), any())
    }


}