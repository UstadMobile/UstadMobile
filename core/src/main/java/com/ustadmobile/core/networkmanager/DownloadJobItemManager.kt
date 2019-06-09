package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.UMUtil

import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DownloadJobItemManager(private val db: UmAppDatabase, val downloadJobUid: Int) {

    private val jobItemUidToStatusMap = HashMap<Int, DownloadJobItemStatus>()

    private val progressChangedItems = HashSet<DownloadJobItemStatus>()

    var onDownloadJobItemChangeListener: OnDownloadJobItemChangeListener? = null

    private val executor = Executors.newSingleThreadScheduledExecutor()

    @Volatile
    var rootItemStatus: DownloadJobItemStatus? = null
        private set

    @Volatile
    var rootContentEntryUid: Long = 0
        private set

    interface OnDownloadJobItemChangeListener {

        fun onDownloadJobItemChange(status: DownloadJobItemStatus?, manager: DownloadJobItemManager)

    }

    init {
        executor.scheduleWithFixedDelay({ this.doCommit() }, 1000, 1000, TimeUnit.MILLISECONDS)
        try {
            executor.schedule({ loadFromDb() }, 0, TimeUnit.SECONDS).get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    private fun loadFromDb() {
        val downloadJob = db.downloadJobDao.findByUid(downloadJobUid.toLong())
        rootContentEntryUid = downloadJob.djRootContentEntryUid
        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "DownloadJobItemManager: load " +
                "Download job uid " + downloadJobUid + " root content entry uid = " +
                rootContentEntryUid)

        val jobItems = db.downloadJobItemDao
                .findStatusByDownlaodJobUid(downloadJobUid.toLong())
        for (status in jobItems) {
            jobItemUidToStatusMap[status.jobItemUid] = status
            if (status.contentEntryUid == rootContentEntryUid)
                rootItemStatus = status
        }

        val joinList = db.downloadJobItemParentChildJoinDao
                .findParentChildJoinsByDownloadJobUids(downloadJobUid)
        for (join in joinList) {
            val parentStatus = jobItemUidToStatusMap[join.djiParentDjiUid.toInt()]
            val childStatus = jobItemUidToStatusMap[join.djiChildDjiUid.toInt()]

            if (parentStatus == null || childStatus == null) {
                throw IllegalStateException("Invalid parent/child join")
            }

            childStatus.addParent(parentStatus)
            parentStatus.addChild(childStatus)
        }
    }

    fun updateProgress(djiUid: Int, bytesSoFar: Long, totalBytes: Long) {
        executor.execute {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Updating ID #" +
                    djiUid + " bytesSoFar = " + bytesSoFar + " totalBytes=" + totalBytes)
            val djStatus = jobItemUidToStatusMap[djiUid]
            if(djStatus != null) {
                val deltaBytesFoFar = bytesSoFar - djStatus.bytesSoFar
                val deltaTotalBytes = totalBytes - djStatus.totalBytes

                djStatus.bytesSoFar = bytesSoFar
                djStatus.totalBytes = totalBytes
                progressChangedItems.add(djStatus)

                onDownloadJobItemChangeListener?.onDownloadJobItemChange(djStatus, this)

                updateParentsProgress(djStatus.jobItemUid, djStatus.parents, deltaBytesFoFar,
                        deltaTotalBytes)
            }
        }
    }

    fun updateStatus(djiUid: Int, status: Int, callback : UmResultCallback<Void?>?) {
        executor.execute {
            val updatedItems = LinkedList<DownloadJobItemStatus>()
            val djStatus = jobItemUidToStatusMap[djiUid]
            if(djStatus != null) {
                updateItemStatusInt(djStatus, status.toByte(), updatedItems)

                runOnAllParents(djStatus.jobItemUid, djStatus.parents) {parent ->
                    var parentChanged = false
                    val parentsChildren = parent.children
                    if(parentsChildren != null) {
                        if(parentsChildren.all { it.status >= JobStatus.COMPLETE_MIN}){
                            updateItemStatusInt(parent, JobStatus.COMPLETE.toByte(), updatedItems)
                            parentChanged = true
                        }
                    }


                    parentChanged
                }

                db.downloadJobItemDao.updateJobItemStatusList(updatedItems)
                db.contentEntryStatusDao.updateDownloadStatusByList(updatedItems)

                val updatedRoot = updatedItems.firstOrNull { it.contentEntryUid == rootContentEntryUid }
                if(updatedRoot != null) {
                    db.downloadJobDao.updateStatus(downloadJobUid, updatedRoot.status)
                }
            }

            callback?.onDone(null)
        }
    }

    private fun updateItemStatusInt(djStatus: DownloadJobItemStatus, status: Byte,
                                    updatedItems: MutableList<DownloadJobItemStatus>) {
        djStatus.status = status
        updatedItems.add(djStatus)
        onDownloadJobItemChangeListener?.onDownloadJobItemChange(djStatus, this)
    }


    private fun updateParentsProgress(djiUid: Int, parents: List<DownloadJobItemStatus>?, deltaBytesFoFar: Long,
                                      deltaTotalBytes: Long) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Updating ID #" +
                djiUid + " parents = " + UMUtil.debugPrintList(parents) +
                " deltaBytesSoFar=" + deltaBytesFoFar + ", deltaTotalBytes=" + deltaTotalBytes)
        runOnAllParents(djiUid, parents) { parent ->
            parent.incrementTotalBytes(deltaTotalBytes)
            parent.incrementBytesSoFar(deltaBytesFoFar)
            progressChangedItems.add(parent)
            onDownloadJobItemChangeListener?.onDownloadJobItemChange(parent, this)
            true
        }
    }

    private fun runOnAllParents(djiUid: Int, startParents: List<DownloadJobItemStatus>?,
                                fn : (DownloadJobItemStatus) -> Boolean) {
        var parents = startParents
        while (parents != null && !parents.isEmpty()) {
            val nextParents = LinkedList<DownloadJobItemStatus>()
            for(parent in parents) {
                val parentParents = parent.parents
                if(fn.invoke(parent) && parentParents != null)
                    nextParents.addAll(parentParents)

            }

            parents = nextParents
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "\tUpdating ID #" +
                    djiUid + " next parents = " + UMUtil.debugPrintList(parents))
        }
    }

    fun insertDownloadJobItems(items: List<DownloadJobItem>, callback: UmResultCallback<Void?>?) {
        executor.execute {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Adding download job items" + UMUtil.debugPrintList(items))
            db.downloadJobItemDao.insertListAndSetIds(items)

            for (item in items) {
                val uidIntObj = Integer.valueOf(item.djiUid.toInt())

                val itemStatus = DownloadJobItemStatus(item)
                jobItemUidToStatusMap[uidIntObj] = itemStatus
                if (item.djiContentEntryUid == rootContentEntryUid)
                    rootItemStatus = itemStatus
            }

            callback?.onDone(null)
        }
    }

    fun insertDownloadJobItemsSync(items: List<DownloadJobItem>) {
        val latch = CountDownLatch(1)
        insertDownloadJobItems(items, object: UmResultCallback<Void?> {
            override fun onDone(result: Void?) {
                latch.countDown()
            }
        })

        try {
            latch.await(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) { /*should not happen */
        }

    }


    fun insertParentChildJoins(joins: List<DownloadJobItemParentChildJoin>,
                               callback: UmResultCallback<Void>?) {
        executor.execute {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Adding parent-child joins" + UMUtil.debugPrintList(joins))
            for (join in joins) {
                val childStatus = jobItemUidToStatusMap[join.djiChildDjiUid.toInt()]
                val parentStatus = jobItemUidToStatusMap[join.djiParentDjiUid.toInt()]
                if (childStatus == null || parentStatus == null) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 420,
                            "Parent child join requested: but child or parent uids invalid: $join")
                    throw IllegalArgumentException("Parent child join requested: but child or parent uids invalid")
                }

                if (join.djiChildDjiUid == join.djiParentDjiUid) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 420,
                            "Parent child join requested: child uid = parent uid: $join")
                    throw IllegalArgumentException("childItemUid = parentItemUid")
                }

                childStatus.addParent(parentStatus)
                parentStatus.addChild(childStatus)

                updateParentsProgress(childStatus.jobItemUid, Arrays.asList(parentStatus),
                        childStatus.bytesSoFar, childStatus.totalBytes)
            }

            db.downloadJobItemParentChildJoinDao.insertList(joins)

            callback?.onDone(null)
        }
    }


    fun findStatusByContentEntryUid(contentEntryUid: Long, callback: UmResultCallback<DownloadJobItemStatus>) {
        executor.execute {
            var statusFound = null as DownloadJobItemStatus?
            for (status in jobItemUidToStatusMap.values) {
                if (status.contentEntryUid == contentEntryUid) {
                    statusFound = status
                    break
                }
            }

            callback.onDone(statusFound)
        }
    }

    fun commit(callback: UmResultCallback<Void>) {
        executor.execute {
            doCommit()
            callback.onDone(null)
        }
    }

    private fun doCommit() {
        db.downloadJobItemDao.updateDownloadJobItemsProgress(LinkedList(progressChangedItems))
        progressChangedItems.clear()
    }

    fun close() {
        executor.shutdownNow()
    }
}
