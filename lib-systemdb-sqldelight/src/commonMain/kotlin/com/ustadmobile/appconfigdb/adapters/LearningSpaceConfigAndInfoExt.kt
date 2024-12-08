package com.ustadmobile.appconfigdb.adapters

import com.ustadmobile.systemdb.model.LearningSpaceConfigAndInfo
import systemdb.data.LearningSpaceEntity

fun LearningSpaceConfigAndInfo.asEntity(
    lsUid: Long
) = LearningSpaceEntity(
    lsUid = lsUid,
    lsUrl = info.url,
    lsName = info.name,
    lsDescription = info.description,
    lsLastModified = info.lastModified,
    lsDbUrl = config.dbUrl,
    lsDbUsername = config.dbUsername,
    lsDbPassword = config.dbPassword,
)
