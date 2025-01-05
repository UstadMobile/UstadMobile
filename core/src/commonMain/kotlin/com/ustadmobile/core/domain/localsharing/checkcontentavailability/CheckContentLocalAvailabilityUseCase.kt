package com.ustadmobile.core.domain.localsharing.checkcontentavailability

import com.ustadmobile.lib.db.entities.ContentEntryVersion

/**
 * Check if the given contentEntryVersion can be downloaded locally: requires that all URLs in the
 * manifest are available locally (e.g. by checking the distributed cache available urls hashtable)
 */
interface CheckContentLocalAvailabilityUseCase {

    /**
     * @param contentEntryVersion ContentEntryVersion to check local availability of
     *
     * @return true if all URLs in the manifest are available for local download, false otherwise
     */
    suspend operator fun invoke(contentEntryVersion: ContentEntryVersion): Boolean

}