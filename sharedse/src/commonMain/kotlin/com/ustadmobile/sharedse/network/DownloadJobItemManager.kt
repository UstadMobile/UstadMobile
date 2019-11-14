package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.UMUtil
import kotlinx.coroutines.*
import kotlin.jvm.Volatile

class DownloadJobItemManager(private val db: UmAppDatabase, val downloadJobUid: Int,
                             private val coroutineScope: CoroutineDispatcher) {

    private val jobItemUidToStatusMap = HashMap<Int, DownloadJobItemStatus>()

    private val progressChangedItems = HashSet<DownloadJobItemStatus>()

    var onDownloadJobItemChangeListener: (status: DownloadJobItemStatus?, djUid: Int) -> Unit = {status, uid -> Unit}

    @Volatile
    var rootItemStatus: DownloadJobItemStatus? = null
        private set

    @Volatile
    var rootContentEntryUid: Long = 0
        private set

    val size: Int
        get()  = jobItemUidToStatusMap.size


    private lateinit var loadJob: Deferred<Unit>

    init {
        loadJob = GlobalScope.async {
            withContext(coroutineScope){
                //TODO: fix this to check a variable
                loadFromDb()
            }
        }

        GlobalScope.launch {
            withContext(coroutineScope) {
                while (true) {
                    delay(1000)
                    commit()
                }
            }
        }
    }

    /**
     * Loading is done in a suspended method. Most methods are also suspended, and linked to a
     * single threaded scope. As loading is the first thing that gets executed, any suspended
     * function can be relied upon to return the correct result right away. If something is accessing
     * a property, like the root content entry UID, then it might be required to explicitly
     * wait for the loadFromDb method to have finished
     */
    internal suspend fun awaitLoaded() {
        loadJob.await()
    }

    private fun loadFromDb() {
        val downloadJob = db.downloadJobDao.findByUid(downloadJobUid)
        rootContentEntryUid = downloadJob?.djRootContentEntryUid?: 0L
        UMLog.l(UMLog.DEBUG, 420, "DownloadJobItemManager: load " +
                "Download job uid " + downloadJobUid + " root content entry uid = " +
                rootContentEntryUid)

        val jobItems = db.downloadJobItemDao
                .findStatusByDownlaodJobUid(downloadJobUid)
        for (status in jobItems) {
            jobItemUidToStatusMap[status.jobItemUid] = status
            if (status.contentEntryUid == rootContentEntryUid)
                rootItemStatus = status
        }

        val joinList = db.downloadJobItemParentChildJoinDao
                .findParentChildJoinsByDownloadJobUids(downloadJobUid)
        for (join in joinList) {
            val parentStatus = jobItemUidToStatusMap[join.djiParentDjiUid]
            val childStatus = jobItemUidToStatusMap[join.djiChildDjiUid]

            if (parentStatus == null || childStatus == null) {
                throw IllegalStateException("Invalid parent/child join")
            }

            childStatus.addParent(parentStatus)
            parentStatus.addChild(childStatus)
        }
    }

    suspend fun updateProgress(djiUid: Int, bytesSoFar: Long, totalBytes: Long) {
        withContext(coroutineScope) {
//            UMLog.l(UMLog.DEBUG, 420, "Updating ID #" +
//                    djiUid + " bytesSoFar = " + bytesSoFar + " totalBytes=" + totalBytes)
            val djStatus = jobItemUidToStatusMap[djiUid]
            if (djStatus != null) {
                val deltaBytesFoFar = bytesSoFar - djStatus.bytesSoFar
                val deltaTotalBytes = totalBytes - djStatus.totalBytes

                djStatus.bytesSoFar = bytesSoFar
                djStatus.totalBytes = totalBytes
                progressChangedItems.add(djStatus)

                onDownloadJobItemChangeListener(djStatus, downloadJobUid)

                updateParentsProgress(djStatus.jobItemUid, djStatus.parents, deltaBytesFoFar,
                        deltaTotalBytes)
            }
        }
    }

    suspend fun updateStatus(djiUid: Int, status: Int) {
        withContext(coroutineScope) {
            val updatedItems = mutableListOf<DownloadJobItemStatus>()
            val djStatus = jobItemUidToStatusMap[djiUid]
            if (djStatus != null) {
                updateItemStatusInt(djStatus, status, updatedItems)

                runOnAllParents(djStatus.jobItemUid, djStatus.parents) { parent ->
                    var parentChanged = false
                    val parentsChildren = parent.children
                    if (parentsChildren != null) {
                        if (parentsChildren.all { it.status >= JobStatus.COMPLETE_MIN }) {
                            updateItemStatusInt(parent, JobStatus.COMPLETE, updatedItems)
                            parentChanged = true
                        }
                    }


                    parentChanged
                }

                db.downloadJobItemDao.updateJobItemStatusList(updatedItems)
                db.contentEntryStatusDao.updateDownloadStatusByList(updatedItems)

                val updatedRoot = updatedItems.firstOrNull { it.contentEntryUid == rootContentEntryUid }
                if (updatedRoot != null) {
                    db.downloadJobDao.updateStatus(downloadJobUid, updatedRoot.status)
                }
            }
        }
    }

    suspend fun updateJobAndItemStatus(newInactiveStatus: Int, newActiveStatus: Int = -1) {
        withContext(coroutineScope) {
            jobItemUidToStatusMap.forEach {
                if(newInactiveStatus != -1 && it.value.status < JobStatus.WAITING_MIN) {
                    it.value.status = newInactiveStatus
                }
            }
            db.downloadJobDao.updateStatus(downloadJobUid, newInactiveStatus)
            db.downloadJobItemDao.updateJobItemStatusList(jobItemUidToStatusMap.values.toList())
        }
    }

    private fun updateItemStatusInt(djStatus: DownloadJobItemStatus, status: Int,
                                    updatedItems: MutableList<DownloadJobItemStatus>) {
        djStatus.status = status
        updatedItems.add(djStatus)
        onDownloadJobItemChangeListener(djStatus, downloadJobUid)
    }


    private fun updateParentsProgress(djiUid: Int, parents: List<DownloadJobItemStatus>?, deltaBytesFoFar: Long,
                                      deltaTotalBytes: Long) {
//        UMLog.l(UMLog.DEBUG, 420, "Updating ID #" +
//                djiUid + " parents = " + UMUtil.debugPrintList(parents) +
//                " deltaBytesSoFar=" + deltaBytesFoFar + ", deltaTotalBytes=" + deltaTotalBytes)
        runOnAllParents(djiUid, parents) { parent ->
            parent.incrementTotalBytes(deltaTotalBytes)
            parent.incrementBytesSoFar(deltaBytesFoFar)
            progressChangedItems.add(parent)
            onDownloadJobItemChangeListener(parent, downloadJobUid)
            true
        }
    }

    private fun runOnAllParents(djiUid: Int, startParents: List<DownloadJobItemStatus>?,
                                fn: (DownloadJobItemStatus) -> Boolean) {
        var parents = startParents
        while (parents != null && !parents.isEmpty()) {
            val nextParents = mutableListOf<DownloadJobItemStatus>()
            for (parent in parents) {
                val parentParents = parent.parents
                if (fn.invoke(parent) && parentParents != null)
                    nextParents.addAll(parentParents)

            }

            parents = nextParents
            UMLog.l(UMLog.DEBUG, 420, "\tUpdating ID #" +
                    djiUid + " next parents = " + UMUtil.debugPrintList(parents))
        }
    }

    suspend fun insertDownloadJobItems(items: List<DownloadJobItem>/*, callback: UmResultCallback<Void?>?*/) {
        withContext(coroutineScope){
            UMLog.l(UMLog.DEBUG, 420, "Adding download job items" + UMUtil.debugPrintList(items))
            db.downloadJobItemDao.insertListAndSetIds(items)

            for (item in items) {
                val uidIntObj = item.djiUid

                val itemStatus = DownloadJobItemStatus(item)
                jobItemUidToStatusMap[uidIntObj] = itemStatus
                if (item.djiContentEntryUid == rootContentEntryUid)
                    rootItemStatus = itemStatus
            }
        }
    }


    suspend fun insertParentChildJoins(joins: List<DownloadJobItemParentChildJoin>/*,
                               callback: UmResultCallback<Void>?*/) {
        withContext(coroutineScope) {
            UMLog.l(UMLog.DEBUG, 420, "Adding parent-child joins" + UMUtil.debugPrintList(joins))
            for (join in joins) {
                val childStatus = jobItemUidToStatusMap[join.djiChildDjiUid.toInt()]
                val parentStatus = jobItemUidToStatusMap[join.djiParentDjiUid.toInt()]
                if (childStatus == null || parentStatus == null) {
                    UMLog.l(UMLog.ERROR, 420,
                            "Parent child join requested: but child or parent uids invalid: $join")
                    throw IllegalArgumentException("Parent child join requested: but child or parent uids invalid")
                }

                if (join.djiChildDjiUid == join.djiParentDjiUid) {
                    UMLog.l(UMLog.ERROR, 420,
                            "Parent child join requested: child uid = parent uid: $join")
                    throw IllegalArgumentException("childItemUid = parentItemUid")
                }

                childStatus.addParent(parentStatus)
                parentStatus.addChild(childStatus)

                updateParentsProgress(childStatus.jobItemUid, /*Arrays.asList(parentStatus),*/ listOf(parentStatus),
                        childStatus.bytesSoFar, childStatus.totalBytes)
            }

            db.downloadJobItemParentChildJoinDao.insertList(joins)
        }
    }


    suspend fun findStatusByContentEntryUid(contentEntryUid: Long) :DownloadJobItemStatus? {
        return withContext(coroutineScope) {
            var statusFound = null as DownloadJobItemStatus?
            for (status in jobItemUidToStatusMap.values) {
                if (status.contentEntryUid == contentEntryUid) {
                    statusFound = status
                    break
                }
            }

            statusFound
        }
    }

    suspend fun commit() {
        withContext(coroutineScope) {
            doCommit(progressChangedItems.toList())
            progressChangedItems.clear()
        }
    }

    private fun doCommit(itemsToCommit: List<DownloadJobItemStatus>) {
        db.downloadJobItemDao.updateDownloadJobItemsProgress(itemsToCommit)
    }

    fun close() {
        //TODO: shutdown our executor to release the thread
        //executor.shutdownNow()
    }
}
