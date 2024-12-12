package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.systemdb.datasource.SystemDbDataSource
import com.ustadmobile.systemdb.sqlite.SystemDb
import com.ustadmobile.xxhashkmp.XXStringHasher

class SystemDbDataSourceSqlDelight(
    private val systemDb: SystemDb,
    private val xxStringHasher: XXStringHasher
): SystemDbDataSource {

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceInfoDataSourceSqlDelight(systemDb.learningSpaceQueries, xxStringHasher)
    }

}
