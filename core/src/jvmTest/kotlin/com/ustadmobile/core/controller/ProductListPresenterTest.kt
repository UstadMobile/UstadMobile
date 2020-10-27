
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ProductListView
import com.ustadmobile.core.view.ProductDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ProductListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ProductListView

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
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()
        val testEntity = Product().apply {
            //set variables here
            productName = "Cow"
            productActive = true
            productUid = db.productDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = ProductListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(repoProductDaoSpy, timeout(5000)).findAllActiveProductWithInventoryCount()
        verify(mockView, timeout(5000)).list = any()

    }


}