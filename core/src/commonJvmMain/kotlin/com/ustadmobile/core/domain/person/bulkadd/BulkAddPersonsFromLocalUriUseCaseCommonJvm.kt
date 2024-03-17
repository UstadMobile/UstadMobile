package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readString

class BulkAddPersonsFromLocalUriUseCaseCommonJvm(
    private val bulkAddPersonsUseCase: BulkAddPersonsUseCase,
    private val uriHelper: UriHelper,
): BulkAddPersonsFromLocalUriUseCase {

    override suspend fun invoke(
        uri: DoorUri,
        onProgress: BulkAddPersonsUseCase.BulkAddOnProgress,
    ): BulkAddPersonsUseCase.BulkAddUsersResult {
        val csvString = withContext(Dispatchers.IO) {
            uriHelper.openSource(uri).use {
                it.readString()
            }
        }

        return bulkAddPersonsUseCase(
            csv = csvString,
            onProgress = onProgress,
        )
    }

}