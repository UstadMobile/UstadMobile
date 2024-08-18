package com.ustadmobile.lib.rest.domain.learningspace.create

import com.ustadmobile.centraldb.CentralDb
import com.ustadmobile.centraldb.entities.LearningSpaceConfig
import com.ustadmobile.centraldb.entities.LearningSpaceInfo
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.internal.decodeByReader

class CreateLearningSpaceUseCase(
    private val xxStringHasher: XXStringHasher,
    private val centralDb: CentralDb,
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
        //centralDb.learningSpaceInfoDao().upsertAsync(request.info)
        centralDb.learningSpaceConfigDao().insertAsync(
            LearningSpaceConfig(
                lscUid = xxStringHasher.hash(request.url),
                lscDbUrl = request.dbUrl,
                lscDbUsername = request.dbUsername,
                lscDbPassword = request.dbPassword,
            )
        )
    }


}