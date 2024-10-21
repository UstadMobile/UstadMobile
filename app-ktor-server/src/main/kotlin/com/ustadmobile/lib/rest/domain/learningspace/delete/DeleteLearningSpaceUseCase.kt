package com.ustadmobile.lib.rest.domain.learningspace.delete

import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class DeleteLearningSpaceUseCase(
    private val learningSpaceServerRepo: LearningSpaceServerRepo,
    private val di: DI,
) {

    @Serializable
    data class DeleteLearningSpaceUseCase(
        val url: String,
    )

    operator fun invoke(request: DeleteLearningSpaceUseCase) {

        learningSpaceServerRepo.delete(
               request.url

        )
        val dirToDelete:File by di.on(request.url).instance<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT)
        if (dirToDelete.isDirectory){
            dirToDelete.deleteRecursively()
        }
    }


}