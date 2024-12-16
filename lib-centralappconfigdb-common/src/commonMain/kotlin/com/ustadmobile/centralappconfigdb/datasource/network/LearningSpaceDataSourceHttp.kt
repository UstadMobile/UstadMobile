package com.ustadmobile.centralappconfigdb.datasource.network

import com.ustadmobile.centralappconfigdb.datasource.LearningSpaceDataSource
import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LearningSpaceDataSourceHttp(
    private val url: String,
    private val httpClient: HttpClient,
): LearningSpaceDataSource {

    override fun getAll(): Flow<List<LearningSpaceInfo>> {
        return flow {
            try {
                val learningSpaceInfos: List<LearningSpaceInfo> = httpClient.get(
                    "${url}getAll"
                ).body()
                emit(learningSpaceInfos)
            }catch(e: Exception) {
                emit(emptyList())
            }
        }
    }

    override fun upsertLearningSpaceInfo(learningSpaceInfo: List<LearningSpaceInfo>): Int {
        throw IllegalStateException("Remote updates to learning space info NOT allowed")
    }
}