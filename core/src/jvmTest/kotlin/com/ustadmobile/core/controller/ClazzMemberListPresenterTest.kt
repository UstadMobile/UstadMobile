
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzMemberListView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertPersonWithRole
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzMemberListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzMemberListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzMemberDaoSpy: ClazzMemberDao

    private lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        accountManager = di.direct.instance()

        db = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)

        repoClazzMemberDaoSpy = spy(db.clazzMemberDao)
        whenever(db.clazzMemberDao).thenReturn(repoClazzMemberDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenActiveUserDoesNotHaveAddPermissions_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnViewAndSetAddVisibleToFalse() {
        //TODO: insert any entities that are used only in this test
        val testEntity = ClazzMember().apply {
            //set variables here
            clazzMemberUid = db.clazzMemberDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>(ARG_FILTER_BY_CLAZZUID to "42")
        val presenter = ClazzMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzMemberDaoSpy, timeout(5000)).findByClazzUidAndRole(42L,
            ClazzMember.ROLE_STUDENT)
        verify(repoClazzMemberDaoSpy, timeout(5000)).findByClazzUidAndRole(42L,
                ClazzMember.ROLE_TEACHER)

        verify(mockView, timeout(5000)).list = any()
        verify(mockView, timeout(5000)).studentList = any()

        verify(mockView, timeout(5000)).addStudentVisible = false
        verify(mockView, timeout(5000)).addTeacherVisible = false
    }

    @Test
    fun givenActiveAccountHasAddPermissions_whenOnCreateCalled_thenShouldSetAddOptionsToBeVisible() {
        val testClazz = Clazz("Test clazz").apply {
            clazzUid = db.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = db.personDao.insert(this)
        }

        runBlocking {
            db.insertPersonWithRole(activePerson,
            Role().apply {
                rolePermissions = Role.PERMISSION_CLAZZ_ADD_STUDENT or Role.PERMISSION_CLAZZ_ADD_TEACHER
            }, EntityRole().apply {
                erTableId = Clazz.TABLE_ID
                erEntityUid = testClazz.clazzUid
            })
        }

        val endpointUrl = accountManager.activeAccount.endpointUrl
        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", endpointUrl, activePerson.firstNames, activePerson.lastName)

        val presenterArgs = mapOf<String,String>(ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString())
        val presenter = ClazzMemberListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).addStudentVisible = true
        verify(mockView, timeout(5000)).addTeacherVisible = true
    }
}