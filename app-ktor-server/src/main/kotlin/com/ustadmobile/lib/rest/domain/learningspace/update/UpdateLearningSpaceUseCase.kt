package com.ustadmobile.lib.rest.domain.learningspace.update

import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import kotlinx.serialization.Serializable

class UpdateLearningSpaceUseCase(
    private val learningSpaceServerRepo: LearningSpaceServerRepo,
) {

    @Serializable
    data class UpdateLearningSpaceUseCase(
        val url: String,
        val title: String,
        val dbUrl: String,
        val dbUsername: String,
        val dbPassword: String,
        val adminUsername: String,
        val adminPassword: String,
    )

    suspend operator fun invoke(request: UpdateLearningSpaceUseCase) {

        learningSpaceServerRepo.update(
            LearningSpaceConfigAndInfo(
                config = LearningSpaceConfig(
                    lscUrl = request.url,
                    lscDbUrl = request.dbUrl,
                    lscDbUsername = request.dbUsername,
                    lscDbPassword = request.dbPassword,
                ),
                info = LearningSpaceInfo(
                    lsiUrl = request.url,
                    lsiName = request.title,
                )
            )
        )
    }


}