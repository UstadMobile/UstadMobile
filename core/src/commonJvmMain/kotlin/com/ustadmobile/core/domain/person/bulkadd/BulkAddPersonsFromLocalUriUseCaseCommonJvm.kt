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
        accountPersonUid: Long,
        onProgress: BulkAddPersonsUseCase.BulkAddOnProgress,
    ): BulkAddPersonsUseCase.BulkAddUsersResult {
        val csvString = withContext(Dispatchers.IO) {
            val size = uriHelper.getSize(uri)
            if(size > MAX_IMPORT_SIZE) {
                throw IllegalArgumentException("File too big")
            }

            uriHelper.openSource(uri).use {
                it.readString()
            }
        }

        return bulkAddPersonsUseCase(
            csv = csvString,
            onProgress = onProgress,
        )
    }

    companion object {

        //Limit to 10MB
        const val MAX_IMPORT_SIZE = (10 * 1024 * 1024)

    }
}