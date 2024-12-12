package com.ustadmobile.appconfigdb.adapters

import com.ustadmobile.systemdb.model.LearningSpaceInfo
import systemdb.data.LearningSpaceEntity

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
