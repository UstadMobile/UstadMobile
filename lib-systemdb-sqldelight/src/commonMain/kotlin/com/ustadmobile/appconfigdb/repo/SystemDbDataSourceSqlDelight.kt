package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.systemdb.datasource.SystemDbDataSource
import com.ustadmobile.systemdb.sqlite.SystemDb

class SystemDbDataSourceSqlDelight(
    private val localDataSource: SystemDb,
    private val remoteDataUrl: String,
): SystemDbDataSource {

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceInfoDataSourceSqlDelight(localDataSource.learningSpaceQueries)
    }

}
