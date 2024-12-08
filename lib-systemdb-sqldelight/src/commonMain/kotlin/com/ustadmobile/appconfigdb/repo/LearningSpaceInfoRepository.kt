package com.ustadmobile.appconfigdb.repo

import com.ustadmobile.systemdb.model.LearningSpaceInfo
import kotlinx.coroutines.flow.Flow

interface LearningSpaceInfoRepository {

    fun allLearningSpaces(): Flow<List<LearningSpaceInfo>>

}