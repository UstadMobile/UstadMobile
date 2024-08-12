package com.ustadmobile.core.domain.person.bulkadd

import kotlinx.serialization.Serializable

/**
 * Import a CSV that roughly follows users.csvas specified here:
 *   https://www.imsglobal.org/spec/oneroster/v1p2/bind/csv/#users-csv
 *
 * OneRoster CSVs use separate files for demographics (including date of birth, sex, etc).
 */
interface BulkAddPersonsUseCase {

    @Serializable
    data class BulkAddUsersResult(
        val numImported: Int,
    )

    fun interface BulkAddOnProgress {
        operator fun invoke(numImported: Int, totalRecords: Int)

    }


    suspend operator fun invoke(
        csv: String,
        onProgress: BulkAddOnProgress,
    ): BulkAddUsersResult

}