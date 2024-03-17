package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile

class BulkAddPersonsFromLocalUriUseCaseCommonJvm(
    private val bulkAddPersonsUseCase: BulkAddPersonsUseCase
): BulkAddPersonsFromLocalUriUseCase {

    override suspend fun invoke(
        uri: DoorUri
    ): BulkAddPersonsUseCase.BulkAddUsersResult {
        return bulkAddPersonsUseCase(csv = uri.toFile().readText())
    }

}