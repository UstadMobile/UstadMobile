package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DownloadJobItemDao
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.UMUtil
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.sharedse.network.DownloadJobItemManager
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.takeFrom


interface IDownloadJobPreparer {
    suspend fun prepare(jobItemManager: DownloadJobItemManager, appDatabase: UmAppDatabase,
                        appDatabaseRepo: UmAppDatabase, onProgress: (Int) -> Unit = {})
}

/**
 * This function is responsible to get the Download Job preparer running. On Android this should be
 * done using WorkManager. On other systems this may be done as a normal coroutine launch. This can
 * be a job that will take some time (e.g. when
 */
expect fun requestDownloadPreparation(downloadJobUid: Int, context: Any)

/**
 * This runnable sets up a download job so it's ready to run. It starts from a root content entry uid,
 * and then adds all
 */
class DownloadJobPreparer(val _httpClient: HttpClient = defaultHttpClient(),
                          val statusAfterPreparation: Int = JobStatus.QUEUED) : IDownloadJobPreparer {

    private val fetchEntitiesLimit = 1000

    suspend fun downloadJobContentEntries(contentEntryUid: Long, db: UmAppDatabase, dbRepo: UmAppDatabase): Int {
        val repo = dbRepo as DoorDatabaseSyncRepository
        val endpoint = (dbRepo as DoorDatabaseRepository).endpoint
        var numEntriesReceived = -1

        try {
            val _httpResponse = _httpClient.get<HttpResponse> {
                url {
                    takeFrom(endpoint)
                    encodedPath =
                            "${encodedPath}${repo.dbPath}/ContentEntryDao/getAllEntriesRecursively"
                }
                header("X-nid", repo.clientId)
                parameter("contentEntryUid", contentEntryUid)

                parameter("_limit", fetchEntitiesLimit)

            }
            val _httpResult =
                    _httpResponse.receive<List<ContentEntryWithParentChildJoinAndMostRecentContainer>>()
            val _requestId = _httpResponse.headers.get("X-reqid")?.toInt() ?: -1
            db.containerDao.replaceList(_httpResult
                    .filter { it.mostRecentContainer != null }
                    .map { it.mostRecentContainer as Container }
            )
            _httpClient.get<Unit> {
                url {
                    takeFrom(endpoint)
                    encodedPath =
                            "${encodedPath}${repo.dbPath}/ContentEntryDao/_updateContainer_trkReceived"
                }
                parameter("reqId", _requestId)
            }
            db.contentEntryParentChildJoinDao.replaceList(_httpResult
                    .filter { it.contentEntryParentChildJoin != null }
                    .map { it.contentEntryParentChildJoin as ContentEntryParentChildJoin }
            )
            _httpClient.get<Unit> {
                url {
                    takeFrom(endpoint)
                    encodedPath =
                            "${encodedPath}${repo.dbPath}/ContentEntryDao/_updateContentEntryParentChildJoin_trkReceived"
                }
                parameter("reqId", _requestId)
            }
            db.contentEntryDao.replaceList(_httpResult)
            _httpClient.get<Unit> {
                url {
                    takeFrom(endpoint)
                    encodedPath =
                            "${encodedPath}${repo.dbPath}/ContentEntryDao/_updateContentEntry_trkReceived"
                }
                parameter("reqId", _requestId)
            }

            numEntriesReceived = _httpResult.size
        }catch(e: Exception) {
            UMLog.l(UMLog.ERROR, 0, "Exception preparing", e)
        }finally {

        }

        return numEntriesReceived
    }

    override suspend fun prepare(jobItemManager: DownloadJobItemManager, appDatabase: UmAppDatabase,
                        appDatabaseRepo: UmAppDatabase, onProgress: (Int) -> Unit) {
        val startTime = getSystemTimeInMillis()

        var numItemsFetched = 0
        do {
            numItemsFetched = downloadJobContentEntries(jobItemManager.rootContentEntryUid,
                    appDatabase, appDatabaseRepo)
        }while(numItemsFetched == fetchEntitiesLimit)

        val downloadJobUid = jobItemManager.downloadJobUid
        jobItemManager.awaitLoaded()
        val contentEntryUid = jobItemManager.rootContentEntryUid
        println("Prepare download job for content entry uid: $contentEntryUid")
        UMLog.l(UMLog.DEBUG, 420, "DownloadJobPreparer: start " +
                "entry uid = " + contentEntryUid + " download job uid = " + downloadJobUid)

        val jobItemDao = appDatabase.downloadJobItemDao
        var childItemsToCreate: List<DownloadJobItemDao.DownloadJobItemToBeCreated2>
        val rootEntryContainer = appDatabaseRepo.containerDao
                .getMostRecentContainerForContentEntry(contentEntryUid)
        val rootDownlaodJobItem = DownloadJobItem(
                jobItemManager.downloadJobUid, contentEntryUid,
                rootEntryContainer?.containerUid ?: 0,
                rootEntryContainer?.fileSize ?: 0)

        var numItemsCreated = 1

        jobItemManager.insertDownloadJobItems(listOf(rootDownlaodJobItem))

        val contentEntryUidToDjiUidMap = HashMap<Long, Int>()
        val parentUids = ArrayList<Long>()
        parentUids.add(contentEntryUid)
        contentEntryUidToDjiUidMap[contentEntryUid] = rootDownlaodJobItem.djiUid

        val createdJoinCepjUids = HashSet<Long>()

        appDatabase.contentEntryStatusDao.insertOrAbort(listOf(ContentEntryStatus(contentEntryUid,
                rootEntryContainer == null,
                rootEntryContainer?.fileSize ?: 0)))

        val statusList = mutableListOf<ContentEntryStatus>()
        do {
            statusList.clear()
            childItemsToCreate = jobItemDao.findByParentContentEntryUuids(parentUids)
            UMLog.l(UMLog.DEBUG, 420, "DownloadJobPreparer: found " +
                    childItemsToCreate.size + " child items on from parents " +
                    UMUtil.debugPrintList(parentUids))

            parentUids.clear()

            for (child in childItemsToCreate) {
                if (!contentEntryUidToDjiUidMap.containsKey(child.contentEntryUid)) {
                    val newItem = DownloadJobItem(downloadJobUid,
                            child.contentEntryUid, child.containerUid, child.fileSize)

                    jobItemManager.insertDownloadJobItems(listOf(newItem))
                    numItemsCreated++
                    statusList.add(ContentEntryStatus(child.contentEntryUid,
                            child.fileSize > 0, child.fileSize))

                    contentEntryUidToDjiUidMap[child.contentEntryUid] = newItem.djiUid

                    if (newItem.djiContainerUid == 0L)
                    //this item is a branch, not a leaf if containeruid = 0
                        parentUids.add(child.contentEntryUid)

                    statusList.add(ContentEntryStatus(child.contentEntryUid,
                            child.fileSize > 0, child.fileSize))

                }

                if (!createdJoinCepjUids.contains(child.cepcjUid)) {
                    jobItemManager.insertParentChildJoins(listOf(DownloadJobItemParentChildJoin(
                            contentEntryUidToDjiUidMap[child.parentEntryUid]!!,
                            contentEntryUidToDjiUidMap[child.contentEntryUid]!!,
                            child.cepcjUid)))
                    createdJoinCepjUids.add(child.cepcjUid)
                }
            }

            appDatabase.contentEntryStatusDao.insertOrAbort(statusList)
        } while (!parentUids.isEmpty())
        UMLog.l(UMLog.VERBOSE, 420, "Created " + numItemsCreated +
                " items. Time to prepare download job: " +
                (getSystemTimeInMillis() - startTime) + "ms")
        jobItemManager.commit()
        jobItemManager.updateJobAndItemStatus(statusAfterPreparation)
    }
}
