package com.ustadmobile.core.networkmanager.downloadmanager

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

/**
 * This class manages multiple DownloadJobs, including:
 *   Download queue management using the enqueue, pause, and cancel functions
 *   Adding DownloadJobs and component DownloadJobItems
 *   Progress tracking including propogating progress up the content child-parent relationship tree
 */
@Deprecated("use ContentJobPlugins")
abstract class ContainerDownloadManager {

    abstract suspend fun handleContainerLocalImport(container: Container)

    abstract suspend fun enqueue(downloadJobId: Int)

    abstract suspend fun pause(downloadJobId: Int)

    abstract suspend fun cancel(downloadJobId: Int)

    abstract suspend fun setMeteredDataAllowed(downloadJobUid: Int, meteredDataAllowed: Boolean)

    abstract suspend fun handleConnectivityChanged(status: ConnectivityStatus)

    abstract suspend fun commit()

}