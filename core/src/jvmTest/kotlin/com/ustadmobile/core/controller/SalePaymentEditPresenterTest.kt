
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SalePaymentEditView
import com.ustadmobile.core.view.SalePaymentDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.SalePayment

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SalePaymentEditPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: SalePaymentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoSalePaymentDaoSpy: SalePaymentDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoSalePaymentDaoSpy = spy(clientDbRule.db.salePaymentDao)
        whenever(clientDbRule.db.salePaymentDao).thenReturn(repoSalePaymentDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf<String, String>()

        val presenter = SalePaymentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        //e.g. initialEntity.someNameField = "Bob"

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = clientDbRule.db.salePaymentDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.someNameField)
    }

    @Test
    fun givenExistingSalePayment_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = SalePayment().apply {
            someName = "Spelling Clazz"
            salePaymentUid = clientDbRule.repo.salePaymentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.salePaymentUid.toString())
        val presenter = SalePaymentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            clientDbRule.db.salePaymentDao.findByUidLive(testEntity.salePaymentUid)
                    .waitUntil(5000) { it?.someName == "New Spelling Clazz" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.someName)
    }


}