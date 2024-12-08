package com.ustadmobile.systemdb.model

import kotlinx.coroutines.flow.Flow

interface LearningSpaceRepository {

    fun allLearningSpaces(): Flow<List<LearningSpaceInfo>>

}
