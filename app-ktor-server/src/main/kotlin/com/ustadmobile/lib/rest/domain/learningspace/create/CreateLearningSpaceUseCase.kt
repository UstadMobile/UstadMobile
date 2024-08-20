package com.ustadmobile.lib.rest.domain.learningspace.create

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import kotlinx.serialization.Serializable

class CreateLearningSpaceUseCase(
    private val xxStringHasher: XXStringHasher,
    private val systemDb: SystemDb,
) {

    @Serializable
    data class CreateLearningSpaceRequest(
        val url: String,
        val title: String,
        val dbUrl: String,
        val dbUsername: String,
        val dbPassword: String,
    )

    suspend operator fun invoke(request: CreateLearningSpaceRequest) {
        systemDb.learningSpaceConfigDao().insertAsync(
            LearningSpaceConfig(
                lscUid = xxStringHasher.hash(request.url),
                lscUrl = request.url,
                lscDbUrl = request.dbUrl,
                lscDbUsername = request.dbUsername,
                lscDbPassword = request.dbPassword,
            )
        )
    }


}