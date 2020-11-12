package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DownloadJobItemDao
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJobItemWithParents
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.ext.dbVersionHeader
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.List
import kotlin.collections.joinToString
import kotlin.collections.listOf
import kotlin.collections.mutableListOf
import kotlin.collections.set


interface IDownloadJobPreparer {
    suspend fun prepare(downloadManager: ContainerDownloadManager,
                        appDatabase: UmAppDatabase,
                        appDatabaseRepo: UmAppDatabase, onProgress: (Int) -> Unit)
}

/**
 * This runnable sets up a download job so it's ready to run. It starts from a root content entry uid,
 * and then adds all
 */
class DownloadJobPreparer(val downloadJobUid: Int) : IDownloadJobPreparer {

    override suspend fun prepare(downloadManager: ContainerDownloadManager,
                                 appDatabase: UmAppDatabase,
                        appDatabaseRepo: UmAppDatabase, onProgress: (Int) -> Unit) {
        val startTime = getSystemTimeInMillis()
        val downloadJob = appDatabase.downloadJobDao.findByUid(downloadJobUid)
        if(downloadJob == null) {
            throw IllegalArgumentException("DownloadJobUid does not exist!")
        }

        val entriesToDownload = appDatabaseRepo.contentEntryDao
                .getAllEntriesRecursivelyAsList(downloadJob.djRootContentEntryUid)

        UMLog.l(UMLog.DEBUG, 420, "DownloadJobPreparer: start " +
                "entry uid = " + downloadJob.djRootContentEntryUid + " download job uid = " + downloadJobUid)
        val jobItemDao = appDatabase.downloadJobItemDao
        var childItemsToCreate: List<DownloadJobItemDao.DownloadJobItemToBeCreated2>

        var numItemsCreated = 0

        val rootDownloadJobItem = downloadManager.getDownloadJobRootItem(downloadJobUid)
        if(rootDownloadJobItem == null) {
            throw IllegalStateException("Invalid DownloadJob: DownloadJob has no root entry")
        }

        val contentEntryUidToDjiUidMap = HashMap<Long, Int>()
        val parentUids = ArrayList<Long>()
        parentUids.add(downloadJob.djRootContentEntryUid)
        contentEntryUidToDjiUidMap[downloadJob.djRootContentEntryUid] = rootDownloadJobItem.djiUid

        val createdJoinCepjUids = HashSet<Long>()

        do {
            childItemsToCreate = jobItemDao.findByParentContentEntryUuids(parentUids)
            UMLog.l(UMLog.DEBUG, 420, "DownloadJobPreparer: found " +
                    childItemsToCreate.size + " child items on from parents " +
                    childItemsToCreate.joinToString())

            parentUids.clear()

            for (child in childItemsToCreate) {
                if (!contentEntryUidToDjiUidMap.containsKey(child.contentEntryUid)) {
                    val parentDjiUid = contentEntryUidToDjiUidMap[child.parentEntryUid] ?: 0
                    val childParentList = if(parentDjiUid != 0) {
                        createdJoinCepjUids.add(child.cepcjUid)
                        mutableListOf(DownloadJobItemParentChildJoin(parentDjiUid, 0, child.cepcjUid))
                    }else {
                        mutableListOf()
                    }

                    val newItem = DownloadJobItemWithParents(downloadJob,
                            child.contentEntryUid, child.containerUid, child.fileSize,
                            childParentList)

                    downloadManager.addItemsToDownloadJob(listOf(newItem))
                    numItemsCreated++
                    contentEntryUidToDjiUidMap[child.contentEntryUid] = newItem.djiUid

                    if (newItem.djiContainerUid == 0L)
                    //this item is a branch, not a leaf if containeruid = 0
                        parentUids.add(child.contentEntryUid)

                }

                //TODO: REALLY! - handle the case whne a child already has a download but we need to add another parent
//                if (!createdJoinCepjUids.contains(child.cepcjUid)) {
//                    jobItemManager.insertParentChildJoins(listOf(DownloadJobItemParentChildJoin(
//                            contentEntryUidToDjiUidMap[child.parentEntryUid]!!,
//                            contentEntryUidToDjiUidMap[child.contentEntryUid]!!,
//                            child.cepcjUid)))
//                    createdJoinCepjUids.add(child.cepcjUid)
//                }
            }
        } while (!parentUids.isEmpty())
        UMLog.l(UMLog.VERBOSE, 420, "Created " + numItemsCreated +
                " items. Time to prepare download job: " +
                (getSystemTimeInMillis() - startTime) + "ms")
    }
}
