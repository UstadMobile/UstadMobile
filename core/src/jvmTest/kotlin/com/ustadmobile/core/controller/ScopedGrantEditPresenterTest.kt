
package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScopedGrantDao
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.combinedFlagValue
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.util.commontest.ext.awaitResult
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

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoScopedGrantDaoSpy = spy(repo.scopedGrantDao)
        whenever(repo.scopedGrantDao).thenReturn(repoScopedGrantDaoSpy)

        testNavController = di.direct.instance()

        //setup nav controller as if the user has come from ClazzEdit
        testNavController.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
    }

    fun ScopedGrantEditView.captureBitmaskLiveData() : DoorLiveData<List<BitmaskFlag>>{
        return nullableArgumentCaptor<DoorLiveData<List<BitmaskFlag>>>().run {
            verify(this@captureBitmaskLiveData, timeout(5000)).bitmaskList = capture()
            lastValue!!
        }
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf(
            ScopedGrantEditView.ARG_PERMISSION_LIST to Clazz.TABLE_ID.toString()) + resultArgs
        testNavController.navigate(ScopedGrantEditView.VIEW_NAME, presenterArgs)

        val presenter = ScopedGrantEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!
        val bitmaskFlagLiveData = mockView.captureBitmaskLiveData()

        mockView.stub {
            on { bitmaskList }.thenReturn(bitmaskFlagLiveData)
        }

        //add some permission
        val bitmaskList = bitmaskFlagLiveData.getValue()?.toMutableList()
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_ASSIGNMENT_SELECT }?.enabled = true
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_CLAZZ_ADD_STUDENT }?.enabled = true

        presenter.handleClickSave(initialEntity)

        val initialFlags = bitmaskFlagLiveData.getValue()!!
        ScopedGrantEditPresenter.PERMISSION_LIST_MAP[Clazz.TABLE_ID]!!.forEach { flag ->
            Assert.assertTrue("Bitmask flag list contains $flag", initialFlags.any {
                it.flagVal == flag.flagVal
            })
        }

        val result = testNavController.awaitResult(5000, ScopedGrant::class,
            ClazzEdit2View.VIEW_NAME, "ScopedGrant")
        Assert.assertEquals("Saved with expected permissions",
            Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_CLAZZ_ADD_STUDENT,
            result.first().sgPermissions)

    }

    @Test
    fun givenExistingScopedGrant_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = ScopedGrant().apply {
            sgPermissions = (Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_ASSIGNMENT_UPDATE)
        }

        val presenterArgs = mapOf(
            ARG_ENTITY_JSON to Json.encodeToString(ScopedGrant.serializer(), testEntity),
            ScopedGrantEditView.ARG_PERMISSION_LIST to Clazz.TABLE_ID.toString()) + resultArgs
        testNavController.navigate(ScopedGrantEditView.VIEW_NAME, presenterArgs)

        val presenter = ScopedGrantEditPresenter(context, presenterArgs, mockView,
            mockLifecycleOwner, di)

        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        val bitmaskFlagLiveData = mockView.captureBitmaskLiveData()
        mockView.stub {
            on { bitmaskList }.thenReturn(bitmaskFlagLiveData)
        }

        //Serialize to string so we can 'freeze' what we got at the start
        val initialBitmaskFlagJson: String = Json.encodeToString(
            ListSerializer(BitmaskFlag.serializer()), bitmaskFlagLiveData.getValue()!!)

        val bitmaskList = bitmaskFlagLiveData.getValue()?.toMutableList()
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_ASSIGNMENT_SELECT }?.enabled = false
        bitmaskList?.firstOrNull { it.flagVal == Role.PERMISSION_CLAZZ_ADD_TEACHER }?.enabled = true

        presenter.handleClickSave(initialEntity)


        val initialBitmaskFlags : List<BitmaskFlag> = Json.decodeFromString(ListSerializer(
            BitmaskFlag.serializer()), initialBitmaskFlagJson)
        Assert.assertEquals("Initial flags enabled as per arguments",
            (Role.PERMISSION_ASSIGNMENT_SELECT or Role.PERMISSION_ASSIGNMENT_UPDATE),
            initialBitmaskFlags.combinedFlagValue)

        val result = testNavController.awaitResult(5000, ScopedGrant::class,
            ClazzEdit2View.VIEW_NAME, "ScopedGrant")
        Assert.assertEquals("Got expected permissions",
            Role.PERMISSION_CLAZZ_ADD_TEACHER or Role.PERMISSION_ASSIGNMENT_UPDATE,
                result.first().sgPermissions)
    }
}