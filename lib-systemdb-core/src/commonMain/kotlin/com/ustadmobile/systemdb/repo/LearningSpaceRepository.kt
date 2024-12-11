package com.ustadmobile.systemdb.repo

import com.ustadmobile.systemdb.model.LearningSpaceInfo
import kotlinx.coroutines.flow.Flow

interface LearningSpaceRepository {

    fun allLearningSpaces(): Flow<List<LearningSpaceInfo>>

}
