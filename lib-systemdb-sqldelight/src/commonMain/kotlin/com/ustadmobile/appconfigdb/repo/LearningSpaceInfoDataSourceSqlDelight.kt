package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.appconfigdb.adapters.asInfoOnlyEntity
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import systemdb.data.LearningSpaceQueries

class LearningSpaceInfoDataSourceSqlDelight(
    private val learningSpaceQueries: LearningSpaceQueries,
): LearningSpaceDataSource {

    override fun upsertLearningSpaceInfo(learningSpaceInfo: List<LearningSpaceInfo>): Int {
        TODO()
    }

    override fun getAll(): Flow<List<LearningSpaceInfo>> {
        return emptyFlow()
    }

}