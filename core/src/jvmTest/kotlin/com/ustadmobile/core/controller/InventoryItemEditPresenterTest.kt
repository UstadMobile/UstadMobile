
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.InventoryItemEditView
import com.ustadmobile.core.view.InventoryItemDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.InventoryItemDao
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.InventoryItem

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.*
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PRODUCT_UID
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Product
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class InventoryItemEditPresenterTest {

    @@JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: InventoryItemEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoProductDaoSpy: ProductDao

    private lateinit var di: DI

    private lateinit var repoInventoryItemDaoSpy: InventoryItemDao

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

        repoInventoryItemDaoSpy = spy(repo.inventoryItemDao)
        whenever(repo.inventoryItemDao).thenReturn(repoInventoryItemDaoSpy)
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        //1. Create product
        //2. Log in LE
        //3.

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()


        val pinkHat = Product().apply{
            productName = "Pink Hat"
            productActive = true
            productBasePrice = 420F
            productDateAdded = systemTimeInMillis()
            productDesc = "This is a pink hat. Buy me!"
            productUid = db.productDao.insert(this)
        }

        val leOne = Person().apply{
            firstNames = "Le"
            lastName = "One"
            personUid = db.insertPersonOnlyAndGroup(this).personUid
        }

        accountManager.activeAccount.personUid = leOne.personUid



        val presenterArgs = mapOf<String, String>(
                ARG_PRODUCT_UID to pinkHat.productUid.toString())
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val presenter = InventoryItemEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!


        verify(repoInventoryItemDaoSpy, timeout(5000)).findWeStock(pinkHat.productUid,
                loggedInPersonUid)
        verify(mockView, timeout(5000)).producers = any()

//        presenter.handleClickSave(initialEntity)
//
//        val existingEntitiesLive = clientDbRule.db.inventoryItemDao.findAllLive()
//        val entitySaved = runBlocking {
//            existingEntitiesLive.waitUntil { it.size == 1 }
//        }.getValue()!!.first()
//        Assert.assertEquals("Entity was saved to database", "Bob",
//                entitySaved.someNameField)
    }


}