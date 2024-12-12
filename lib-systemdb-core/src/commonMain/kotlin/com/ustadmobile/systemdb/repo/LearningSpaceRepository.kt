package com.ustadmobile.systemdb.repo

import com.ustadmobile.systemdb.datasource.LearningSpaceDataSource
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class LearningSpaceRepository(
    private val local: LearningSpaceDataSource,
    private val remote: LearningSpaceDataSource,
): LearningSpaceDataSource {

    override fun upsertLearningSpaceInfo(learningSpaceInfo: List<LearningSpaceInfo>): Int {
        throw IllegalStateException("Changing LearningSpaceInfo locally NOT supported")
    }

    override fun getAll(): Flow<List<LearningSpaceInfo>> {
        val localFlow = local.getAll()

        return localFlow.onStart {
            val remoteFlow = remote.getAll()
            remoteFlow.collect {
                local.upsertLearningSpaceInfo(it)
            }
        }
    }
}