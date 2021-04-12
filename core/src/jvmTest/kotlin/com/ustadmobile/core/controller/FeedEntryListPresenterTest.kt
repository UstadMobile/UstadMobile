
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.FeedEntryListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.FeedEntryDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class FeedEntryListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: FeedEntryListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoFeedEntryDaoSpy: FeedEntryDao

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var accountManager: UstadAccountManager

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

        accountManager = di.direct.instance()

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        repoFeedEntryDaoSpy = spy(db.feedEntryDao)
        whenever(db.feedEntryDao).thenReturn(repoFeedEntryDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        FeedEntry().apply {
            //set variables here
            db.feedEntryDao.insertList(listOf(this))
        }

        val presenter = FeedEntryListPresenter(context, mapOf(), mockView, di,
            mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoFeedEntryDaoSpy, timeout(5000)).findByPersonUidAsDataSource(
            accountManager.activeAccount.personUid)
        verify(mockView, timeout(5000)).list = any()
        verify(repoFeedEntryDaoSpy, timeout(5000)).getFeedSummary(
            accountManager.activeAccount.personUid)
        //TODO: verify any other properties that the presenter should set on the view
    }

}