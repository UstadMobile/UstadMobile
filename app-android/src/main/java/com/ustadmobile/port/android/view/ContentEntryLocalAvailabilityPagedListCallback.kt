package com.ustadmobile.port.android.view

import androidx.paging.PagedList
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.view.ext.activeRange
import java.util.concurrent.atomic.AtomicReference

class ContentEntryLocalAvailabilityPagedListCallback(private val localAvailabilityManager: LocalAvailabilityManager,
                                                     var pagedList: PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?,
                                                     private var onEntityAvailabilityChanged: ((Map<Long, Boolean>) -> Unit)?) : PagedList.Callback() {

    private val availabilityMonitorRequest = AtomicReference<AvailabilityMonitorRequest?>()

    private val activeRange = AtomicReference(Pair(-1, -1))

    override fun onChanged(position: Int, count: Int) {
        handleActiveRangeChanged()
    }

    override fun onInserted(position: Int, count: Int) {
        handleActiveRangeChanged()
    }

    override fun onRemoved(position: Int, count: Int) {
        handleActiveRangeChanged()
    }

    private fun handleActiveRangeChanged() {
        val currentPagedList = pagedList
        val currentActiveRange = currentPagedList?.activeRange()
        if (currentPagedList != null && currentActiveRange != null
                && !activeRange.compareAndSet(currentActiveRange, currentActiveRange)) {
            val containerUidsToMonitor = (currentActiveRange.first until currentActiveRange.second)
                    .fold(mutableListOf<Long>(), { uidList, index ->
                        val contentEntry = currentPagedList[index]
                        if (contentEntry != null && contentEntry.leaf) {
                            val mostRecentContainerUid = contentEntry.mostRecentContainer?.containerUid
                                    ?: 0L
                            if (mostRecentContainerUid != 0L) {
                                uidList += mostRecentContainerUid
                            }
                        }
                        uidList
                    })
            val entityAvailabilityChangeVal = onEntityAvailabilityChanged
            val newRequest = if (containerUidsToMonitor.isNotEmpty() && entityAvailabilityChangeVal != null) {
                AvailabilityMonitorRequest(containerUidsToMonitor, entityAvailabilityChangeVal)
            } else {
                null
            }
            val oldRequest = availabilityMonitorRequest.getAndSet(newRequest)
            if (oldRequest != null) {
                localAvailabilityManager.removeMonitoringRequest(oldRequest)
            }

            if (newRequest != null) {
                localAvailabilityManager.addMonitoringRequest(newRequest)
            }
        }
    }

    fun onDestroy() {
        val currentRequest = availabilityMonitorRequest.getAndSet(null)

        if (currentRequest != null) {
            localAvailabilityManager.removeMonitoringRequest(currentRequest)
        }

        pagedList = null
        onEntityAvailabilityChanged = null
    }

}