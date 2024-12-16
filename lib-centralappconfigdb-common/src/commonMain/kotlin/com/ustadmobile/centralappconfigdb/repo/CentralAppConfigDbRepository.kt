package com.ustadmobile.centralappconfigdb.repo

import com.ustadmobile.centralappconfigdb.datasource.LearningSpaceDataSource
import com.ustadmobile.centralappconfigdb.datasource.CentralAppConfigDbDataSource

/**
 *
 */
class CentralAppConfigDbRepository(
    private val local: CentralAppConfigDbDataSource,
    private val remote: CentralAppConfigDbDataSource,
): CentralAppConfigDbDataSource {

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceRepository(local.learningSpaceDataSource, remote.learningSpaceDataSource)
    }

}