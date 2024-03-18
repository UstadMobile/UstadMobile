package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.door.DoorUri

interface BulkAddPersonsFromLocalUriUseCase {

    suspend operator fun invoke(
        uri: DoorUri,
        accountPersonUid: Long,
        onProgress: BulkAddPersonsUseCase.BulkAddOnProgress,
    ): BulkAddPersonsUseCase.BulkAddUsersResult

}