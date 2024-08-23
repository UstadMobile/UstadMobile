package com.ustadmobile.appconfigdb.composites

import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo

data class LearningSpaceConfigAndInfo(
    var config: LearningSpaceConfig = LearningSpaceConfig(),
    var info: LearningSpaceInfo = LearningSpaceInfo(),
)

