package com.ustadmobile.lib.rest.domain.learningspace.update

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import com.ustadmobile.systemdb.model.LearningSpaceConfig
import com.ustadmobile.systemdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.systemdb.model.LearningSpaceInfo
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
                    url = request.url,
                    dbUrl = request.dbUrl,
                    dbUsername = request.dbUsername,
                    dbPassword = request.dbPassword,
                ),
                info = LearningSpaceInfo(
                    url = request.url,
                    name = request.title,
                    description = request.title,
                    lastModified = systemTimeInMillis(),
                )
            )
        )
    }


}