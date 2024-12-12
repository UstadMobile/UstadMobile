package com.ustadmobile.systemdb.repo

import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.systemdb.datasource.SystemDbDataSource

/**
 *
 */
class SystemDbRepository(
    private val local: SystemDbDataSource,
    private val remote: SystemDbDataSource,
): SystemDbDataSource {

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceRepository(local.learningSpaceDataSource, remote.learningSpaceDataSource)
    }

}