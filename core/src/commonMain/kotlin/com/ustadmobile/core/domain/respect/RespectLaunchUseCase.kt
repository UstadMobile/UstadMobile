package com.ustadmobile.core.domain.respect

import com.ustadmobile.lib.db.entities.respect.RespectApp
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.lib.db.entities.respect.RespectLesson

interface RespectLaunchUseCase {

    suspend operator fun invoke(
        app: RespectApp,
        lesson: RespectLesson?,
        assignment: RespectAssignment?,
    )

}