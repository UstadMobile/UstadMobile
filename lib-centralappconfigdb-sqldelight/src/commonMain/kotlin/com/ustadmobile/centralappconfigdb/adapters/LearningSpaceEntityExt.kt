package com.ustadmobile.centralappconfigdb.adapters

import com.ustadmobile.centralappconfigdb.db.LearningSpaceEntity
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfig
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo

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
