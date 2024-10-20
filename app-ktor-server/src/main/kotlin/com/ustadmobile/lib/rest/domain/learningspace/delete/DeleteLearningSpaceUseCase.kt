package com.ustadmobile.lib.rest.domain.learningspace.delete

import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import kotlinx.serialization.Serializable

class DeleteLearningSpaceUseCase(
    private val learningSpaceServerRepo: LearningSpaceServerRepo,
) {

    @Serializable
    data class DeleteLearningSpaceUseCase(
        val url: String,
    )

    operator fun invoke(request: DeleteLearningSpaceUseCase) {

        learningSpaceServerRepo.delete(
               request.url

        )
    }


}