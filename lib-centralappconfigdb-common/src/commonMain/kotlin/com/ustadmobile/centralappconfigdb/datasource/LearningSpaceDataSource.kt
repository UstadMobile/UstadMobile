package com.ustadmobile.centralappconfigdb.datasource

import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import kotlinx.coroutines.flow.Flow

interface LearningSpaceDataSource {

    fun getAll(): Flow<List<LearningSpaceInfo>>

    /**
     * This function is used only by mobile/desktop clients.
     */
    fun upsertLearningSpaceInfo(learningSpaceInfo: List<LearningSpaceInfo>): Int

    companion object {
        const val PATH = "learningspace"
    }

}
