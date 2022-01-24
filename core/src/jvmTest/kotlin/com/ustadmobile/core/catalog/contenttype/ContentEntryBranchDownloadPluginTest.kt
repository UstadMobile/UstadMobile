package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.ContentEntryBranchDownloadPlugin.Companion.CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID
import com.ustadmobile.core.contentjob.ContentPluginIds
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.kodein.di.DI
import org.junit.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.mockito.kotlin.mock

class ContentEntryBranchDownloadPluginTest {

    private lateinit var di: DI

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    private lateinit var rootEntry: ContentEntry

    private lateinit var childLeaf: ContentEntry

    private lateinit var childBranch: ContentEntry

    private val endpoint = Endpoint("http://localhost/")

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)
        val db: UmAppDatabase= di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        //make a contententry with child items
        rootEntry = ContentEntry().apply {
            title = "Root"
            leaf = false
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        childLeaf = ContentEntry().apply {
            title = "Child Leaf"
            leaf = true
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        ContentEntryParentChildJoin().apply {
            cepcjParentContentEntryUid = rootEntry.contentEntryUid
            cepcjChildContentEntryUid = childLeaf.contentEntryUid
            cepcjUid = repo.contentEntryParentChildJoinDao.insert(this)
        }

        childBranch = ContentEntry().apply {
            title = "Child Branch"
            leaf = false
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        ContentEntryParentChildJoin().apply {
            cepcjParentContentEntryUid = rootEntry.contentEntryUid
            cepcjChildContentEntryUid = childBranch.contentEntryUid
            repo.contentEntryParentChildJoinDao.insert(this)
        }

        db.connectivityStatusDao.insert(ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED,
            true, "wifi"))
    }

    @Test
    fun givenContentJobItemCreated_whenRun_thenShouldCreatedChildContentJobItems() {
        val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val contentJob = ContentJob().apply {
            cjUid = runBlocking { db.contentJobDao.insertAsync(this@apply) }
        }

        val contentJobItem = ContentJobItem().apply {
            sourceUri = rootEntry.toDeepLink(endpoint)
            cjiContentEntryUid = rootEntry.contentEntryUid
            cjiUid= runBlocking { db.contentJobItemDao.insertJobItem(this@apply) }
        }


        val processResult = runBlocking {
            ContentEntryBranchDownloadPlugin(Any(), endpoint, di).processJob(
                ContentJobItemAndContentJob().also {
                    it.contentJob = contentJob
                    it.contentJobItem = contentJobItem
                }, mock { }, mock { })
        }

        Assert.assertEquals("Result is done", JobStatus.COMPLETE, processResult.status)
        val jobItemsCreated = runBlocking { db.contentJobItemDao.findNextItemsInQueue(contentJob.cjUid,
            10) }
        val childLeafJob = jobItemsCreated.first {
            it.contentJobItem?.cjiContentEntryUid == childLeaf.contentEntryUid
        }
        Assert.assertEquals("Job created for child leaf has correct plugin",
            ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN, childLeafJob.contentJobItem?.cjiPluginId ?: 0)
        Assert.assertEquals("Job created for child leaf has correct sourceUri",
            childLeaf.toDeepLink(endpoint), childLeafJob.contentJobItem?.sourceUri)

        val childBranchJob = jobItemsCreated.first {
            it.contentJobItem?.cjiContentEntryUid == childBranch.contentEntryUid
        }
        Assert.assertEquals("Job created for child branch has correct plugin",
            CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID, childBranchJob.contentJobItem?.cjiPluginId)

    }

}