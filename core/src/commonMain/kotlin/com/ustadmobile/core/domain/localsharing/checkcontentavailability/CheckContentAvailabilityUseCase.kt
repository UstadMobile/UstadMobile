package com.ustadmobile.core.domain.localsharing.checkcontentavailability

import com.ustadmobile.lib.db.entities.ContentEntryVersion

interface CheckContentAvailabilityUseCase {

    suspend operator fun invoke(contentEntryVersion: ContentEntryVersion): Boolean

}