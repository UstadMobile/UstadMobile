package com.ustadmobile.core.controller


import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance


class LanguageEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: LanguageEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoLanguageDaoSpy: LanguageDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoLanguageDaoSpy = spy(repo.languageDao)
        whenever(repo.languageDao).thenReturn(repoLanguageDaoSpy)


    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val presenterArgs = mapOf<String, String>()
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenter = LanguageEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity.name = "Italian"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntil(5000, listOf("Language")) {
                repo.languageDao.findLanguagesList().size == 1
            }
        }

        val entitySaved = repo.languageDao.findLanguagesList()[0]
        Assert.assertEquals("Entity was saved to database", initialEntity.name,
                entitySaved.name)


    }

    @Test
    fun givenExistingLanguage_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = Language().apply {
            name = "German"
            langUid = repo.languageDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.langUid.toString())
        val presenter = LanguageEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.name = "Russian"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntil(5000, listOf("Language")) {
                repo.languageDao.findByUid(testEntity.langUid)?.name == "Russian"
            }

            val reason = repo.languageDao.findByUidAsync(testEntity.langUid)
            Assert.assertEquals("Name was saved and updated",
                    "Russian", reason!!.name)

        }
    }


}
