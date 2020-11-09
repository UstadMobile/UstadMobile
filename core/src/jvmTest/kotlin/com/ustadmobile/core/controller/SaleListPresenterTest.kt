
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SaleListView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ProductListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Product
import org.junit.Assert
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SaleListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var context: Any

    private lateinit var mockView: SaleListView

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    private lateinit var repoSaleDaoSpy: SaleDao

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
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {

        val db: UmAppDatabase by di.activeDbInstance()
        val accountManager: UstadAccountManager by di.instance<UstadAccountManager>()

        val testEntity = Sale().apply {
            //set variables here

        }

        val presenterArgs = mapOf<String,String>()
        val presenter = SaleListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(repoSaleDaoSpy, timeout(5000)).findAllSales(accountManager.activeAccount.personUid)
        verify(mockView, timeout(5000)).list = any()

    }

    //TODO: add test on click goes to detail

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}