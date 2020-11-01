
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.ProductEditView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ProductDetailPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ProductDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoProductDaoSpy: ProductDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoProductDaoSpy = spy(clientDbRule.db.productDao)
        whenever(clientDbRule.db.productDao).thenReturn(repoProductDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenProductExists_whenOnCreateCalled_thenProductIsSetOnView() {
        val testEntity = Product().apply {
            //set variables here
            productUid = clientDbRule.db.productDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.productUid.toString())
        val presenter = ProductDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.productUid, entityValSet.productUid)
    }

    @Test
    fun givenProductExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val testEntity = Product().apply {
            //set variables here
            productUid = clientDbRule.db.productDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.productUid.toString())
        val presenter = ProductDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        presenter.handleClickEdit()

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ProductEditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.productUid.toString())), any())
    }

}