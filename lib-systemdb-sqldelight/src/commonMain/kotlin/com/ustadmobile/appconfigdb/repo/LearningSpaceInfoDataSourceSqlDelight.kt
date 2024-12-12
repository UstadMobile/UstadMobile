package com.ustadmobile.appconfigdb.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ustadmobile.appconfigdb.adapters.asInfoOnlyEntity
import com.ustadmobile.appconfigdb.adapters.asLearningSpaceConfigAndInfo
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.xxhashkmp.XXStringHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import systemdb.data.LearningSpaceQueries

class LearningSpaceInfoDataSourceSqlDelight(
    private val learningSpaceQueries: LearningSpaceQueries,
    private val xxStringHasher: XXStringHasher,
): LearningSpaceDataSource {

    override fun upsertLearningSpaceInfo(
        learningSpaceInfo: List<LearningSpaceInfo>
    ): Int= learningSpaceQueries.transactionWithResult {
        learningSpaceInfo.forEach {
            learningSpaceQueries.insertFullObject(
                it.asInfoOnlyEntity(xxStringHasher.hash(it.url))
            )
        }
        0
    }

    override fun getAll(): Flow<List<LearningSpaceInfo>> {
        return learningSpaceQueries.selectAll().asFlow().mapToList(Dispatchers.IO).map { learningSpaces ->
            learningSpaces.map { it.asLearningSpaceConfigAndInfo().info }
        }
    }

}