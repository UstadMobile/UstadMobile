package com.ustadmobile.centralappconfigdb.adapters

import com.ustadmobile.centralappconfigdb.db.LearningSpaceEntity
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfigAndInfo

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
