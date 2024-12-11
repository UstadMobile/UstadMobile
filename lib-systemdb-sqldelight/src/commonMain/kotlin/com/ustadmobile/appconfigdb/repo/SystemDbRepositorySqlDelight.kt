package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.systemdb.repo.LearningSpaceRepository
import com.ustadmobile.systemdb.repo.SystemDbRepository
import com.ustadmobile.systemdb.sqlite.SystemDb

class SystemDbRepositorySqlDelight(
    private val systemDb: SystemDb,
    private val url: String,
): SystemDbRepository {

    override val learningSpaceRepository: LearningSpaceRepository by lazy {
        LearningSpaceInfoRepositorySqlDelight(systemDb.learningSpaceQueries)
    }

}
