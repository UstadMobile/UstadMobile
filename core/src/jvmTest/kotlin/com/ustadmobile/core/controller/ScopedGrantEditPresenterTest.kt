
package com.ustadmobile.core.controller


import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScopedGrantDao
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.commontest.ext.awaitResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ScopedGrantEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ScopedGrantEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoScopedGrantDaoSpy: ScopedGrantDao

    private lateinit var testNavController: UstadNavController

    //Arguments that will direct the presenter to save the reuslt to the nav controller
    private val resultArgs = mapOf(UstadView.ARG_RESULT_DEST_VIEWNAME to ClazzEdit2View.VIEW_NAME,
        UstadView.ARG_RESULT_DEST_KEY to "ScopedGrant")

    private lateinit var accountManager: UstadAccountManager

    private lateinit var clazz: Clazz

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

        repo = di.directActiveRepoInstance()

        repoScopedGrantDaoSpy = spy(repo.scopedGrantDao)
        whenever(repo.scopedGrantDao).thenReturn(repoScopedGrantDaoSpy)

        testNavController = di.direct.instance()


        val adminPerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "testadmin"
                admin = true
            })
        }

        clazz = Clazz().apply {
            clazzName = "Test Clazz"
            clazzUid = repo.clazzDao.insert(this)
        }

        runBlocking {
            repo.grantScopedPermission(adminPerson.personGroupUid, Role.ALL_PERMISSIONS,
                ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)
        }

        accountManager = di.direct.instance()
        val userSession = UserSession().apply {
            usPersonUid = adminPerson.personUid
            usStatus = UserSession.STATUS_ACTIVE
        }
        accountManager.activeSession = UserSessionWithPersonAndEndpoint(userSession,
            adminPerson, accountManager.activeEndpoint)

        //setup nav controller as if the user has come from ClazzEdit
        testNavController.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
    }

    fun ScopedGrantEditView.captureBitmaskLiveData(verifyTimes: Int = 1) : DoorLiveData<List<BitmaskFlag>>{
        return nullableArgumentCaptor<DoorLiveData<List<BitmaskFlag>>>().run {
            verify(this@captureBitmaskLiveData, timeout(5000).times(verifyTimes)).bitmaskList = capture()
            lastValue!!
        }
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf(
            ScopedGrantEditView.ARG_GRANT_ON_TABLE_ID to Clazz.TABLE_ID.toString(),
            ScopedGrantEditView.ARG_GRANT_ON_ENTITY_UID to clazz.clazzUid.toString()) + resultArgs
        testNavController.navigate(ScopedGrantEditView.VIEW_NAME, presenterArgs)

        val presenter = ScopedGrantEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!
        val bitmaskFlagLiveData = mockView.captureBitmaskLiveData(2)

        mockView.stub {
            on { bitmaskList }.thenReturn(bitmaskFlagLiveData)
        }

        //add some permission
        val bitmaskList = bitmaskFlagLiveData.getValue()?.toMutableList()
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_ASSIGNMENT_SELECT }?.enabled = true
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_CLAZZ_ADD_STUDENT }?.enabled = true

        presenter.handleClickSave(initialEntity)

        //Make sure the view receives the list of permissions to show as expected
        val initialFlags = bitmaskFlagLiveData.getValue()!!
        ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST.filter {
            ScopedGrantEditPresenter.COURSE_PERMISSIONS.hasFlag(it.flagVal)
        }.forEach { flag ->
            Assert.assertTrue("Bitmask flag list contains $flag", initialFlags.any {
                it.flagVal == flag.flagVal
            })
        }

        //Make sure the result is saved to the database as expected
        runBlocking {
            repo.waitUntil(5000, listOf("ScopedGrant")){
                repo.scopedGrantDao.findByTableIdAndEntityIdSync(Clazz.TABLE_ID, clazz.clazzUid).isNotEmpty()
            }
        }

        val savedEntityInDb = repo.scopedGrantDao.findByTableIdAndEntityIdSync(Clazz.TABLE_ID, clazz.clazzUid)
            .first()

        Assert.assertEquals("Saved with expected permissions",
            Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_CLAZZ_ADD_STUDENT,
            savedEntityInDb.sgPermissions)
    }

    @Test
    fun givenExistingScopedGrant_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = ScopedGrant().apply {
            sgPermissions = (Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_ASSIGNMENT_UPDATE)
            sgEntityUid = clazz.clazzUid
            sgTableId = Clazz.TABLE_ID
            sgUid = runBlocking { repo.scopedGrantDao.insertAsync(this@apply) }
        }

        val presenterArgs = mapOf(
            UstadView.ARG_ENTITY_UID to testEntity.sgUid.toString())
        testNavController.navigate(ScopedGrantEditView.VIEW_NAME, presenterArgs)

        val presenter = ScopedGrantEditPresenter(context, presenterArgs, mockView,
            mockLifecycleOwner, di)

        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        val bitmaskFlagLiveData = mockView.captureBitmaskLiveData(2)
        mockView.stub {
            on { bitmaskList }.thenReturn(bitmaskFlagLiveData)
        }

        //Serialize to string so we can 'freeze' what we got at the start
        val initialBitmaskFlagJson: String = Json.encodeToString(
            ListSerializer(BitmaskFlag.serializer()), bitmaskFlagLiveData.getValue()!!)

        //Change permission so that only PERMISSION_ADD_TEACHER is enabled
        val bitmaskList = bitmaskFlagLiveData.getValue()?.toMutableList()
        bitmaskList?.forEach {
            it.enabled = it.flagVal == Role.PERMISSION_CLAZZ_ADD_TEACHER
        }

        presenter.handleClickSave(initialEntity)


        val initialBitmaskFlags : List<BitmaskFlag> = Json.decodeFromString(ListSerializer(
            BitmaskFlag.serializer()), initialBitmaskFlagJson)

        Assert.assertEquals("Initial flags enabled as per arguments",
            (Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_ASSIGNMENT_UPDATE),
            initialBitmaskFlags.combinedFlagValue)

        runBlocking {
            repo.waitUntil(5000, listOf("ScopedGrant")){
                runBlocking {
                    repo.scopedGrantDao.findByUid(testEntity.sgUid)?.sgPermissions ==
                            Role.PERMISSION_CLAZZ_ADD_TEACHER
                }
            }
        }

        val resultInDb = runBlocking { repo.scopedGrantDao.findByUid(testEntity.sgUid ) }

        Assert.assertEquals("Got expected permissions after change",
            Role.PERMISSION_CLAZZ_ADD_TEACHER, resultInDb?.sgPermissions)
    }
}