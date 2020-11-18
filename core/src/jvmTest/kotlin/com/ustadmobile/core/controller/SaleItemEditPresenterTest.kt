
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SaleItemEditView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.SaleItemDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.SaleItem

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.*
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Product
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SaleItemEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: SaleItemEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    private lateinit var repoSaleItemDaoSpy: SaleItemDao

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

        repoSaleItemDaoSpy = spy(repo.saleItemDao)
        whenever(repo.saleItemDao).thenReturn(repoSaleItemDaoSpy)
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        val presenterArgs = mapOf<String, String>()

        val presenter = SaleItemEditPresenter(context, presenterArgs, mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //e.g. initialEntity.someNameField = "Bob"
        initialEntity.saleItemQuantity = 42
        initialEntity.saleItemPricePerPiece = 420F
        initialEntity.saleItemCreationDate = systemTimeInMillis()

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = db.saleItemDao.findAllActiveLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", 42,
                entitySaved.saleItemQuantity)
    }

    @Test
    fun givenExistingProduct_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()
        val testEntity = SaleItem().apply {
            saleItemQuantity = 42
            saleItemPricePerPiece = 420F
            saleItemCreationDate = systemTimeInMillis()
            saleItemUid = db.saleItemDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.saleItemUid.toString())
        val presenter = SaleItemEditPresenter(context, presenterArgs, mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"
        initialEntity.saleItemQuantity = 84

        presenter.handleClickSave(initialEntity)


        val entitySaved = runBlocking {
            db.saleItemDao.findWithProductByUidLive(testEntity.saleItemUid)
                    .waitUntil(5000) { it?.saleItemQuantity == 84 }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                84, entitySaved!!.saleItemQuantity)
    }


}