package com.ustadmobile.centralappconfigdb.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ustadmobile.centralappconfigdb.adapters.asInfoOnlyEntity
import com.ustadmobile.centralappconfigdb.adapters.asLearningSpaceConfigAndInfo
import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import com.ustadmobile.xxhashkmp.XXStringHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.ustadmobile.centralappconfigdb.db.LearningSpaceQueries

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