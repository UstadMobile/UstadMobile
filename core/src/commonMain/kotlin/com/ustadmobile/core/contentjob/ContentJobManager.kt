package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemParentChildJoin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.coroutines.coroutineContext

/**
 * Manage a given ContentJob.
 */
class ContentJobManager(
    val jobId: Int,
    private val endpoint: Endpoint,
    override val di: DI
) : DIAware, ContentJobProgressListener{

    val runLock = Mutex()

    val modLock = Mutex()

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val jobItems: MutableMap<Int, ContentJobItemAndParents> = mutableMapOf()

    private val liveDataCreatedWeakMap: MutableMap<ContentJobItemLiveData, ContentJobItemLiveData> = mutableMapOf()

    class ContentJobProgress(val deltaProgress: Long, val deltaSize: Long, val newStatus: Int = -1)

    private class ContentJobItemLiveData(val jobItemId: Int): DoorMutableLiveData<ContentJobItem?>()

    private inner class ContentJobItemAndParents(var jobItem: ContentJobItem, var parents: List<ContentJobItemAndParents>) {

        var needsCommit: Boolean = false

        fun onProgress(progress: ContentJobProgress) {
            val newItem = jobItem.copy().apply {
                cjiProgress += progress.deltaProgress
                cjiTotal += progress.deltaSize
            }

            jobItem = newItem

            liveDataCreatedWeakMap.keys.filter { it.jobItemId == newItem.cjiUid }.forEach {
                it.sendValue(newItem)
            }

            parents.forEach {
                it.onProgress(progress)
            }
        }

        fun updateBranchStatus() {
            //TODO: Run a query to determine if this branch is finished (e.g. all child items are
            //finished)
        }

        fun addNewParent(newParent: ContentJobItemAndParents) {
            parents += newParent
            newParent.onProgress(ContentJobProgress(jobItem.cjiProgress, jobItem.cjiTotal))
        }

    }



    private suspend fun getOrLoadJobItem(jobItemId: Int, justCreated: ContentJobItem? = null) : ContentJobItemAndParents{
        val alreadyLoadedItem = jobItems[jobItemId]
        if(alreadyLoadedItem != null)
            return alreadyLoadedItem



        //load the item itself e.g. dao.get

        //get a list of parent child joins, load any of them not already loaded (recursive)
        TODO("")
    }


    suspend fun insertJobItem(contentJobItem: ContentJobItem) {
        //insert the new job item
    }

    suspend fun insertParentChildJoin(contentJobItemParentChildJoin: ContentJobItemParentChildJoin) {
        //childItem = getOrLoadJobItem
        //parentItem = getOrLoadJobItem

        //childItem.addNewParent
    }

    fun onJobItemUpdated(jobItem: ContentJobItem) {
        //getOrLoadJobItem.update
    }

    override fun onProgress(contentJobItem: ContentJobItem) {
        //getOrLoad the item itself, calculate the delta, then fire onProgress

    }

    suspend fun getLiveStatus(jobItemId: Int): DoorLiveData<ContentJobItem?> {
        val liveData = ContentJobItemLiveData(jobItemId)
        GlobalScope.async {
            //get or load it, then send
        }

        return liveData
    }

    suspend fun runJob() {
        withContext(Dispatchers.Default) {
            //TODO: Use runLock to ensure that this is not running more than once





        }
    }


}