package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.argWhere
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertPersonWithRole
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ClazzDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

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
    }

    @Test
    fun givenUserHasAttendancePermissions_whenOnCreateCalled_thenShouldMakeTabVisible() {
        val accountManager: UstadAccountManager = di.direct.instance()
        val db: UmAppDatabase = di.on(accountManager.activeAccount)
                .direct.instance(tag = UmAppDatabase.TAG_DB)

        val testEntity = Clazz().apply {
            clazzUid = db.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Officer"
            lastName = "Jones"
            username = "officer"
            personUid = db.insertPersonOnlyAndGroup(this).personUid

        }

        val roleWithAttendancePermission = Role().apply {
            roleName = "Officer"
            rolePermissions = Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or Role.PERMISSION_CLAZZ_SELECT
        }

        val endpointUrl = accountManager.activeAccount.endpointUrl
        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", endpointUrl, activePerson.firstNames, activePerson.lastName)

        runBlocking { db.insertPersonWithRole(activePerson, roleWithAttendancePermission,
                EntityRole().apply {
                    erTableId = Clazz.TABLE_ID
                    erEntityUid = testEntity.clazzUid
                }) }

        val presenter = ClazzDetailPresenter(Any(),
                mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)

        presenter.onCreate(null)

        verify(mockView, timeout(5000)).tabs = argWhere {
            it.any { it.startsWith(ClazzLogListAttendanceView.VIEW_NAME) }
        }
    }

    @Test
    fun givenUserDoesNotHaveAttendancePermissions_whenOnCreateCalled_thenTabsSetWithoutAttendance() {
        val accountManager: UstadAccountManager = di.direct.instance()
        val db: UmAppDatabase = di.on(accountManager.activeAccount)
                .direct.instance(tag = UmAppDatabase.TAG_DB)

        val testEntity = Clazz().apply {
            clazzUid = db.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Officer"
            lastName = "Jones"
            username = "officer"
            personUid = db.personDao.insert(this)
        }

        val presenter = ClazzDetailPresenter(Any(),
                mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)

        presenter.onCreate(null)

        verify(mockView, timeout(5000)).tabs = argWhere {
            !it.any { it.startsWith(ClazzLogListAttendanceView.VIEW_NAME) }
        }

    }
}