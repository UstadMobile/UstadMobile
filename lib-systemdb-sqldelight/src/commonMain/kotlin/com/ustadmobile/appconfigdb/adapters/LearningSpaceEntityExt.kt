package com.ustadmobile.appconfigdb.adapters

import com.ustadmobile.systemdb.model.LearningSpaceConfig
import com.ustadmobile.systemdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import systemdb.data.LearningSpaceEntity

fun LearningSpaceEntity.asLearningSpaceConfigAndInfo()= LearningSpaceConfigAndInfo(
    info = LearningSpaceInfo(
        url = lsUrl,
        name = lsName,
        description = lsDescription,
        lastModified = lsLastModified
    ),
    config = LearningSpaceConfig(
        url = lsUrl,
        dbUrl = lsDbUrl,
        dbUsername = lsDbUsername,
        dbPassword = lsDbPassword
    )
)
