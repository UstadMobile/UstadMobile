
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.core.view.SaleDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SaleItemDao
import com.ustadmobile.core.db.dao.SaleDeliveryDao
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Sale

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.*
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ProductEditView
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SaleEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var mockView: SaleEditView

    private lateinit var repoProductDaoSpy: ProductDao

    private lateinit var di: DI

    private lateinit var repoSaleDaoSpy: SaleDao
    private lateinit var repoSaleItemDaoSpy: SaleItemDao
    private lateinit var repoSaleDeliveryDaoSpy: SaleDeliveryDao
    private lateinit var repoSalePaymentDaoSpy: SalePaymentDao

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

        repoSaleDaoSpy = spy(repo.saleDao)
        whenever(repo.saleDao).thenReturn(repoSaleDaoSpy)

        repoSaleItemDaoSpy = spy(repo.saleItemDao)
        whenever(repo.saleItemDao).thenReturn(repoSaleItemDaoSpy)

        repoSaleDeliveryDaoSpy = spy(repo.saleDeliveryDao)
        whenever(repo.saleDeliveryDao).thenReturn(repoSaleDeliveryDaoSpy)

        repoSalePaymentDaoSpy = spy(repo.salePaymentDao)
        whenever(repo.salePaymentDao).thenReturn(repoSalePaymentDaoSpy)
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()
        val presenterArgs = mapOf<String, String>()

        val presenter = SaleEditPresenter(context, presenterArgs, mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity.saleActive = true
        initialEntity.saleCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        initialEntity.saleDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
        initialEntity.saleNotes = "Cow says mooo"
        initialEntity.saleDiscount = 21

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = db.saleDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Cow says moo",
                entitySaved.saleNotes)
    }

//    @Test
//    fun givenExistingSale_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
//
//        val testEntity = Sale().apply {
//            someName = "Spelling Clazz"
//            saleUid = clientDbRule.repo.saleDao.insert(this)
//        }
//
//        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.saleUid.toString())
//        val presenter = SaleEditPresenter(context,
//                presenterArgs, mockView, mockLifecycleOwner,
//                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
//                clientDbRule.accountLiveData)
//        presenter.onCreate(null)
//
//        val initialEntity = mockView.captureLastEntityValue()!!
//
//        //Make some changes to the entity (e.g. as the user would do using data binding)
//        //e.g. initialEntity!!.someName = "New Spelling Clazz"
//
//        presenter.handleClickSave(initialEntity)
//
//        val entitySaved = runBlocking {
//            clientDbRule.db.saleDao.findByUidLive(testEntity.saleUid)
//                    .waitUntil(5000) { it?.someName == "New Spelling Clazz" }.getValue()
//        }
//
//        Assert.assertEquals("Name was saved and updated",
//                "New Spelling Clazz", entitySaved!!.someName)
//    }


}