package com.ustadmobile.centralappconfigdb.adapters

import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import com.ustadmobile.centralappconfigdb.db.LearningSpaceEntity

fun LearningSpaceInfo.asInfoOnlyEntity(
    lsUid: Long
) = LearningSpaceEntity(
    lsUid = lsUid,
    lsUrl = url,
    lsName = name,
    lsDescription = description,
    lsLastModified = lastModified,
    lsDbUrl = "",
    lsDbUsername = null,
    lsDbPassword = null,
)
