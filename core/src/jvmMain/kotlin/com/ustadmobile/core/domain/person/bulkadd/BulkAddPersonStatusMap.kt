package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.libcache.util.concurrentSafeMapOf

/**
 * When bulk imports are submitted by web clients, the actual import will be running in a Quartz job.
 * The client JS will make periodic requests to track the status. BulkAddPersonStatusMap is held in
 * the DI to allow the HTTP endpoint to get the status of a given job (without the need to use the
 * database).
 */
class BulkAddPersonStatusMap {

    private val jobMap = concurrentSafeMapOf<Long, BulkAddPersonRunImportUiState>()

    operator fun get(startTimestamp: Long): BulkAddPersonRunImportUiState? {
        return jobMap[startTimestamp]
    }

    operator fun set(startTimestamp: Long, state: BulkAddPersonRunImportUiState) {
        jobMap[startTimestamp] = state
    }
}