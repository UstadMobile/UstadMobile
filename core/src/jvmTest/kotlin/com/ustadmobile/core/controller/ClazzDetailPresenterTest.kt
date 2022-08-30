package com.ustadmobile.core.controller

import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }
    }

    @Test
    fun givenUserHasAttendancePermissions_whenOnCreateCalled_thenShouldMakeTabVisible() {
        val accountManager: UstadAccountManager = di.direct.instance()
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = Clazz().apply {
            clazzUid = repo.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Officer"
            lastName = "Jones"
            username = "officer"
            personUid = repo.insertPersonOnlyAndGroup(this).personUid

        }

        val endpointUrl = accountManager.activeEndpoint.url
        accountManager.startLocalTestSessionBlocking(activePerson, endpointUrl)

        runBlocking {
            repo.grantScopedPermission(activePerson,
                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or Role.PERMISSION_CLAZZ_SELECT,
                Clazz.TABLE_ID, testEntity.clazzUid)
        }

        val presenter = ClazzDetailPresenter(Any(),
                mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)

        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).tabs = argWhere {
            it.any { it.startsWith(ClazzLogListAttendanceView.VIEW_NAME) }
        }
    }

    @Test
    fun givenUserDoesNotHaveAttendancePermissions_whenOnCreateCalled_thenTabsSetWithoutAttendance() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = Clazz().apply {
            clazzUid = repo.clazzDao.insert(this)
        }

        val activePerson = Person().apply {
            firstNames = "Officer"
            lastName = "Jones"
            username = "officer"
            personUid = repo.personDao.insert(this)
        }

        val presenter = ClazzDetailPresenter(Any(),
                mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)

        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).tabs = argWhere {
            !it.any { it.startsWith(ClazzLogListAttendanceView.VIEW_NAME) }
        }

    }
}