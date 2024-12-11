package com.ustadmobile.appconfigdb.composites

import androidx.room.Embedded
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import kotlinx.serialization.Serializable

data class LearningSpaceConfigAndInfo(
    @Embedded
    var config: LearningSpaceConfig = LearningSpaceConfig(),
    @Embedded
    var info: LearningSpaceInfo = LearningSpaceInfo(),
)

