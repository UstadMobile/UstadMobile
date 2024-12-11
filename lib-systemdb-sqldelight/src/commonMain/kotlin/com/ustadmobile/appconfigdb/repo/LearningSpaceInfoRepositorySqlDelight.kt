package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.systemdb.model.LearningSpaceInfo
import com.ustadmobile.systemdb.repo.LearningSpaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import systemdb.data.LearningSpaceQueries

class LearningSpaceInfoRepositorySqlDelight(
    private val learningSpaceQueries: LearningSpaceQueries
): LearningSpaceRepository {

    override fun allLearningSpaces(): Flow<List<LearningSpaceInfo>> {
        return emptyFlow()
    }

}