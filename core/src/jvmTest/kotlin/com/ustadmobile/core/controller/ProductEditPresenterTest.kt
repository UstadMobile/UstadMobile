
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.ProductDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Product

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.*
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ProductEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ProductEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoProductDaoSpy: ProductDao

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

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoProductDaoSpy = spy(repo.productDao)
        whenever(repo.productDao).thenReturn(repoProductDaoSpy)
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        val presenterArgs = mapOf<String, String>()

        val presenter = ProductEditPresenter(context, presenterArgs, mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //e.g. initialEntity.someNameField = "Bob"
        initialEntity.productDesc = "Aloha"
        initialEntity.productName = "Mangoes"

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = db.productDao.findAllActiveRolesLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Mangoes",
                entitySaved.productName)
    }

    @Test
    fun givenExistingProduct_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()
        val testEntity = Product().apply {
            productName = "Mangoes"
            productUid = db.productDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.productUid.toString())
        val presenter = ProductEditPresenter(context, presenterArgs, mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"
        initialEntity.productName = "Thai Mangoes"

        presenter.handleClickSave(initialEntity)


        val entitySaved = runBlocking {
            db.productDao.findByUidLive(testEntity.productUid)
                    .waitUntil(5000) { it?.productName == "Thai Mangoes" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "Thai Mangoes", entitySaved!!.productName)
    }


}